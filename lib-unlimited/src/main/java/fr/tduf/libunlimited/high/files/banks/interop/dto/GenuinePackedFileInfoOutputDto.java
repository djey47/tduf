package fr.tduf.libunlimited.high.files.banks.interop.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Output object for packed file information, from .net cli, BANK-I command.
 */
@JsonTypeName("genuinePackedFileInfoOutput")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenuinePackedFileInfoOutputDto {
    @JsonProperty("name")
    private String name;

    @JsonProperty("fileSize")
    private int fileSize;

    public String getName() {
        return name;
    }

    public int getFileSize() {
        return fileSize;
    }
}