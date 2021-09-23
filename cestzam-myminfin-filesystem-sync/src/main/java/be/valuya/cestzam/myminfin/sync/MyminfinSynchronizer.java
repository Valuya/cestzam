package be.valuya.cestzam.myminfin.sync;

import be.valuya.cestzam.api.client.MyminfinApiClient;
import be.valuya.cestzam.api.client.CestzamApiClientBuilder;
import be.valuya.cestzam.api.client.domain.MyminfinDocumentFilter;
import be.valuya.cestzam.api.client.domain.MyminfinDocumentStream;
import be.valuya.cestzam.api.login.Capacity;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocument;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentGroup;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentKey;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentProvider;
import be.valuya.cestzam.api.service.myminfin.mandate.MyMinfinMandate;
import be.valuya.cestzam.api.service.myminfin.mandate.MyminfinMandateType;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUser;
import be.valuya.cestzam.api.util.ResultPage;
import be.valuya.cestzam.myminfin.sync.config.LogHandler;
import be.valuya.cestzam.myminfin.sync.config.MyMinfinSynchronizerConfig;
import be.valuya.cestzam.myminfin.sync.config.MyMinfinSynchronizerConfigValidator;
import be.valuya.cestzam.myminfin.sync.domain.SyncResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MyminfinSynchronizer {

    private final MyminfinApiClient myminfinClient;
    private final LogHandler logHandler;
    private MyMinfinSynchronizerConfig config;
    private final Logger logger;

    public MyminfinSynchronizer(MyMinfinSynchronizerConfig config) {
        logger = Logger.getLogger("cestzam.myminfin.sync");
        logHandler = new LogHandler();
        logger.addHandler(logHandler);

        this.config = config;
        MyMinfinSynchronizerConfigValidator.validateConfig(config);

        URI cestzamApiUri = config.getCestzamApiUri();
        myminfinClient = CestzamApiClientBuilder.create()
                .apiUri(cestzamApiUri)
                .build()
                .getMyminfin();
    }

    public SyncResult synchronize() {
        SyncResult syncResult = new SyncResult();

        LocalDateTime startTime = LocalDateTime.now();
        syncResult.setStartTime(startTime);

        myminfinClient.authenticateWithToken(
                config.getUser(), config.getPassword(),
                config.getAuthTokens(), Capacity.ENTERPRISE);
        MyminfinUser myminfinUser = myminfinClient.getAuthenticatedUser();
        logger.log(Level.INFO, MessageFormat.format(
                "Authenticated with myminfin user {0} as {1}",
                myminfinUser.getName(), myminfinUser.getVisitorType()
        ));

        List<MyMinfinMandate> citizenMandates = myminfinClient.getAvailableMandates(MyminfinMandateType.CITIZEN);
        logger.log(Level.FINE, MessageFormat.format(
                "Listed {0,number,#} citizen mandates",
                citizenMandates.size()
        ));

        List<MyMinfinMandate> mandatesToProcess = getMandatesToProcess(citizenMandates);
        logger.log(Level.INFO, MessageFormat.format(
                "{0,number,#} citizen mandates to process",
                citizenMandates.size()
        ));
        syncResult.setMandatesToProcess(mandatesToProcess.size());

        int mandatesProcessedCount = 0;
        long documentSyncCount = 0;
        long documentWriteCount = 0;
        long documentBytesWriteCount = 0;

        for (MyMinfinMandate mandate : mandatesToProcess) {
            logger.log(Level.FINE, MessageFormat.format(
                    "Activating mandate {0}",
                    mandate.getMandateeName()
            ));

            myminfinClient.setActiveMandate(mandate);
            MyminfinUser userWithMandate = myminfinClient.getAuthenticatedUser();
            logger.log(Level.INFO, MessageFormat.format(
                    "Activated mandate for customer {0}",
                    userWithMandate.getCustomerName()
            ));

            ResultPage<MyminfinDocumentProvider> providers = myminfinClient.listDocumentsProviders();
            logger.log(Level.FINE, MessageFormat.format(
                    "Listed {0} document providers",
                    providers.getTotalCount()
            ));

            List<MyminfinDocumentProvider> providersToProcess = getProvidersToProcess(providers);
            logger.log(Level.INFO, MessageFormat.format(
                    "{0,number,#} document providers to process",
                    providersToProcess.size()
            ));

            for (MyminfinDocumentProvider documentProvider : providersToProcess) {
                MyminfinDocumentFilter documentFilter = new MyminfinDocumentFilter();
                documentFilter.setProvider(documentProvider.getValue());

                MyminfinDocumentFilter completeFilter = completeDocumentFilter(documentFilter);
                ResultPage<MyminfinDocument> documentResultPage = myminfinClient.searchDocuments(completeFilter);
                List<MyminfinDocument> documentList = documentResultPage.getPageItems();
                logger.log(Level.FINE, MessageFormat.format(
                        "Listed {0} documents for provider {1}",
                        documentList.size(), documentProvider.getValue()
                ));

                for (MyminfinDocument document : documentList) {
                    Optional<Long> writtenBytesOptional = syncDocument(mandate, documentProvider, document);
                    documentSyncCount += 1;
                    if (writtenBytesOptional.isPresent()) {
                        Long writtenBytes = writtenBytesOptional.get();
                        documentWriteCount += 1;
                        documentBytesWriteCount += writtenBytes;
                    }
                }
            }

            logger.log(Level.INFO, MessageFormat.format(
                    "Completed all documents for mandate {0}",
                    mandate.getMandatorName()
            ));
            mandatesProcessedCount += 1;
        }

        logger.log(Level.INFO, "Completed all mandates");


        syncResult.setMandatesProcessed(mandatesProcessedCount);
        syncResult.setDocumentSynchronized(documentSyncCount);
        syncResult.setDocumentWritten(documentWriteCount);
        syncResult.setDocumentBytesWritten(documentBytesWriteCount);
        syncResult.setEndTime(LocalDateTime.now());
        syncResult.setCompleted(true);
        ArrayList<LogRecord> logEntries = new ArrayList<>(logHandler.getLastRecords());
        syncResult.setLogEntries(logEntries);
        return syncResult;
    }

    private Optional<Long> syncDocument(MyMinfinMandate mandate, MyminfinDocumentProvider documentProvider, MyminfinDocument document) {
        Path mandatorTargetPath = getMandateTargetPath(mandate);
        Path documentPath = getDocumentPath(documentProvider, document);
        Optional<Path> documentFilePathOptional = syncIsRequired(mandatorTargetPath, documentPath, document.getDocumentKey());
        if (documentFilePathOptional.isPresent()) {
            Path docFilePath = documentFilePathOptional.get();
            long writtenBytes = downloadDocument(document, mandatorTargetPath, docFilePath);
            writeCestzamFile(document, mandatorTargetPath, docFilePath);
            return Optional.of(writtenBytes);
        } else {
            return Optional.empty();
        }
    }

    private void writeCestzamFile(MyminfinDocument document, Path mandatorTargetPath, Path docFilePath) {
        Path fileName = docFilePath.getFileName();
        Path docDirPath = docFilePath.getParent();
        String cestzamFileName = fileName.toString() + ".cestzam";
        Path cestzamFilepath = docDirPath.resolve(cestzamFileName);
        Path absoluteCestzamFilePath = mandatorTargetPath.resolve(cestzamFilepath);
        logger.fine(MessageFormat.format(
                "Writing cestzam file  {0}", absoluteCestzamFilePath
        ));

        try (OutputStream outputStream = Files.newOutputStream(cestzamFilepath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            String uniqueId = document.getDocumentKey().toUniqueIdString();
            String fileContent = uniqueId + "\n";
            byte[] fileBytes = fileContent.getBytes(StandardCharsets.UTF_8);
            new ByteArrayInputStream(fileBytes)
                    .transferTo(outputStream);
        } catch (IOException e) {
            throw new MyminfinSyncException(e);
        }
    }


    private long downloadDocument(MyminfinDocument document, Path mandatorTargetPath, Path docFilePath) {
        Path absoluteDocumentPath = mandatorTargetPath.resolve(docFilePath);
        logger.fine(MessageFormat.format(
                "Downloading document {0} to {1}", document.getDocumentKey(), absoluteDocumentPath
        ));
        MyminfinDocumentStream myminfinDocumentStream = myminfinClient.downloadDocument(document.getDocumentKey());
        try (OutputStream outputStream = Files.newOutputStream(absoluteDocumentPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            long writtenBytes = myminfinDocumentStream.getInputStream()
                    .transferTo(outputStream);
            myminfinDocumentStream.getInputStream().close();

            logger.fine(MessageFormat.format(
                    "Downloaded {0} bytes for document {1}", writtenBytes, document.getDocumentKey()
            ));

            return writtenBytes;
        } catch (IOException e) {
            throw new MyminfinSyncException(e);
        }
    }

    private Optional<Path> syncIsRequired(Path mandatorTargetPath, Path documentPath, MyminfinDocumentKey documentKey) {
        boolean mandatorPathExists = Files.exists(mandatorTargetPath);
        if (!mandatorPathExists) {
            logger.fine(MessageFormat.format(
                    "Creating mandator directory {0}", mandatorTargetPath
            ));
            try {
                Files.createDirectories(mandatorTargetPath);
            } catch (IOException e) {
                throw new MyminfinSyncException("Unable to create mandator directory " + mandatorTargetPath, e);
            }
        }

        boolean mandatorPathWritable = Files.isWritable(mandatorTargetPath);
        if (!mandatorPathWritable) {
            throw new MyminfinSyncException("Mandator directory not writable: " + mandatorTargetPath);
        }

        Path documentFilePath = mandatorTargetPath.resolve(documentPath);
        Path documentDirectoryPath = documentFilePath.getParent();
        boolean docDirectoryExists = Files.exists(documentDirectoryPath);
        if (!docDirectoryExists) {
            logger.fine(MessageFormat.format(
                    "Creating document directory {0}", mandatorTargetPath
            ));
            try {
                Files.createDirectories(documentDirectoryPath);
            } catch (IOException e) {
                throw new MyminfinSyncException("Unable to create document directory " + documentDirectoryPath, e);
            }
        }

        boolean docDirectoryWritable = Files.isWritable(documentDirectoryPath);
        if (!docDirectoryWritable) {
            throw new MyminfinSyncException("Document directory not writable: " + documentDirectoryPath);
        }

        // Need to store document key to be sure we have a unique id
        Path documentCestzamFilePath = getDocumentSeameFilePath(documentFilePath, documentDirectoryPath);
        boolean docFileExists = Files.exists(documentFilePath);
        boolean cestzamFileExists = Files.exists(documentCestzamFilePath);

        if (docFileExists && cestzamFileExists) {
            String documentCestzamFileKey = getCestzamFileContent(documentCestzamFilePath);
            if (documentCestzamFileKey.equals(documentKey.toUniqueIdString())) {
                logger.fine(MessageFormat.format("Cestzam file present: not syncing document key {0}", documentKey));
                return Optional.empty();
            } else {
                logger.fine(MessageFormat.format("Cestzam file present: Another document key ({0}) uses same filename ({1}) than our document key {2}. Using another filename",
                        documentCestzamFileKey, documentFilePath, documentKey));
                Path nextFileNamePath = getNextDocumentFilename(documentDirectoryPath, documentFilePath);
                return syncIsRequired(mandatorTargetPath, nextFileNamePath, documentKey);
            }
        } else if (docFileExists) {
            logger.warning(MessageFormat.format(
                    "A document exsists at {0} but does not appear to be handled by cestzam (.cestzam file missing). Overwriting it for document key {1}",
                    documentFilePath, documentKey));
            try {
                Files.delete(documentFilePath);
            } catch (IOException e) {
                throw new MyminfinSyncException("Unable to delete document " + documentFilePath, e);
            }
            return Optional.of(documentFilePath);
        } else {
            return Optional.of(documentFilePath);
        }
    }

    private Path getNextDocumentFilename(Path documentDirectoryPath, Path documentFilePath) {
        String curFileName = documentFilePath.getFileName().toString();

        String extensionLessName = curFileName.replaceAll("\\.pdf$", "")
                .strip();
        Pattern digitsPattern = Pattern.compile("\\(([0-9]+)\\)$");
        Matcher matcher = digitsPattern.matcher(extensionLessName.strip());
        if (matcher.matches()) {
            String digitGroup = matcher.group(1);
            int curDigit = Integer.parseInt(digitGroup);
            String strippedFileName = curFileName.replaceAll("\\(" + digitGroup + "\\)", "").strip();
            return getNextDocumentFilename(documentDirectoryPath, strippedFileName, curDigit);
        } else {
            return getNextDocumentFilename(documentDirectoryPath, extensionLessName, 1);
        }
    }

    private Path getNextDocumentFilename(Path documentDirectoryPath, String extensionLessName, int index) {
        return documentDirectoryPath.resolve(MessageFormat.format("{0} ({1,number,#}).pdf",
                extensionLessName, index));
    }

    private String getCestzamFileContent(Path documentCestzamFilePath) {
        try {
            List<String> lines = Files.readAllLines(documentCestzamFilePath);
            if (lines.size() < 1) {
                throw new MyminfinSyncException("Empty seame file at " + documentCestzamFilePath);
            }
            return lines.get(0);
        } catch (IOException e) {
            throw new MyminfinSyncException("Unable to read cestzam file at " + documentCestzamFilePath, e);
        }
    }

    private Path getDocumentSeameFilePath(Path documentFilePath, Path documentDirectoryPath) {
        return documentDirectoryPath.resolve(documentFilePath.getFileName().toString() + ".cestzam");
    }

    private Path getDocumentPath(MyminfinDocumentProvider documentProvider, MyminfinDocument document) {
        String documentGroupname = getDocumentGroupName(documentProvider, document);
        String documentYearName = document.getDocumentYear().toString();
        String documentFileName = getDocumentFileName(document);
        Path documentRelativePath = Paths.get(documentGroupname, documentYearName, documentFileName);
        return documentRelativePath;
    }

    private String getDocumentFileName(MyminfinDocument document) {
        String documentTitle = document.getDocumentTitle();
        LocalDate documentDate = document.getDocumentDate();
        String isoDate = documentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String fileName = MessageFormat.format("{0} {1}.pdf", isoDate, documentTitle);
        return fileName;
    }

    private String getDocumentGroupName(MyminfinDocumentProvider documentProvider, MyminfinDocument document) {
        // group_tva
        List<String> documentGroups = document.getDocumentGroups();
        // provider unique group: group_ov_pri
        List<MyminfinDocumentGroup> candidatesGroups = documentProvider.getGroupList()
                .stream()
                .filter(providerGroup -> documentGroups.contains(providerGroup.getValue()))
                .collect(Collectors.toList());

        if (candidatesGroups.size() > 1) {
            Collections.sort(candidatesGroups, Comparator.comparing(MyminfinDocumentGroup::getLabel));
            int candidatesCount = candidatesGroups.size();
            String groupLabel = candidatesGroups.get(0).getLabel();
            String message = MessageFormat.format("{0} groupes correspondent au document {1}. Le document sera placé dans le dossier du premier groupe: {2}.",
                    candidatesCount, document.getDocumentTitle(), groupLabel);
            logger.warning(message);
            return groupLabel;
        } else if (candidatesGroups.size() == 1) {
            return candidatesGroups.get(0).getLabel();
        } else {
            String ungroupedLabel = "default";
            String message = MessageFormat.format("Aucun groupe trouvé pour le document {0}. Le document sera placé dans le dossier {1}.",
                    document.getDocumentTitle(), ungroupedLabel);
            logger.warning(message);
            return ungroupedLabel;
        }
    }

    private Path getMandateTargetPath(MyMinfinMandate mandate) {
        String mandatorName = mandate.getMandatorName();
        Path targetPath = config.getTargetPath();
        return targetPath.resolve(mandatorName);
    }

    private MyminfinDocumentFilter completeDocumentFilter(MyminfinDocumentFilter documentFilter) {
        LocalDate documentFromDate = config.getDocumentFromDate();
        if (documentFromDate != null) {
            documentFilter.setDocumentDateFrom(documentFromDate);
        }

        return documentFilter;
    }

    private List<MyminfinDocumentProvider> getProvidersToProcess(ResultPage<MyminfinDocumentProvider> providers) {
        Pattern documentProviderNamePattern = config.getDocumentProviderNamePattern();
        if (documentProviderNamePattern != null) {
            return providers.getPageItems()
                    .stream()
                    .filter(p -> documentProviderNamePattern.matcher(p.getValue()).matches())
                    .collect(Collectors.toList());
        }

        return providers.getPageItems();
    }

    private List<MyMinfinMandate> getMandatesToProcess(List<MyMinfinMandate> citizenMandates) {
        String singleMandatorNationalNumber = config.getSingleMandatorNationalNumber();
        if (singleMandatorNationalNumber != null) {
            return citizenMandates.stream()
                    .filter(m -> singleMandatorNationalNumber.equalsIgnoreCase(m.getMandatorNationalNumber()))
                    .collect(Collectors.toList());
        }

        Pattern mandatorNameRegex = config.getMandatorNamePattern();
        if (mandatorNameRegex != null) {
            return citizenMandates.stream()
                    .filter(m -> mandatorNameRegex.matcher(m.getMandatorName()).matches())
                    .collect(Collectors.toList());
        }

        return citizenMandates;
    }
}
