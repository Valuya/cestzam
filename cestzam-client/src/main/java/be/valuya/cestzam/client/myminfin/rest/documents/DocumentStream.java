package be.valuya.cestzam.client.myminfin.rest.documents;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;

@Getter
@AllArgsConstructor
public class DocumentStream {
    private String fileName;
    private String mimeType;
    private InputStream inputStream;
}
