package be.valuya.cestzam.myminfin.sync.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.LogRecord;

@Getter
@Setter
public class SyncResult {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;

    private int mandatesToProcess;
    private int mandatesProcessed;

    private long documentSynchronized;
    private long documentWritten;
    private long documentBytesWritten;

    private List<LogRecord> logEntries;
}
