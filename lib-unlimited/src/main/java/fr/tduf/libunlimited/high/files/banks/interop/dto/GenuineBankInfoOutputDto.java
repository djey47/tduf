package fr.tduf.libunlimited.high.files.banks.interop.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

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