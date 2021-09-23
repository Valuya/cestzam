package be.valuya.cestzam.api.client.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyminfinDocumentStream {
    private InputStream inputStream;
    private String mimeType;
    private String fileName;
}
