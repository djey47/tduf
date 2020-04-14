package fr.tduf.libunlimited.high.files.banks.interop.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Output object for packed file information, from .net cli, BANK-I command.
 */
@JsonTypeName("genuinePackedFileInfoOutput")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenuinePackedFileInfoOutputDto {
    @JsonProperty("name")
    private String name;

    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("fileSize")
    private int fileSize;

    @JsonProperty("type")
    private String type;

    public String getName() {
        return name;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getShortName() {
        return shortName;
    }

    public String getType() {
        return type;
    }
}