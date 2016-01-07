package fr.tduf.libunlimited.high.files.banks.interop.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.List;

/**
 * Output object from .net cli, BANK-I command.
 */
@JsonTypeName("genuineBankInfoOutput")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenuineBankInfoOutputDto {

    @JsonProperty("year")
    private int year;

    @JsonProperty("fileSize")
    private int fileSize;

    @JsonProperty("packedFiles")
    private List<GenuinePackedFileInfoOutputDto> packedFiles;

    public List<GenuinePackedFileInfoOutputDto> getPackedFiles() {
        return packedFiles;
    }

    public int getYear() {
        return year;
    }

    public int getFileSize() {
        return fileSize;
    }
}