package fr.tduf.libunlimited.low.files.banks.dto;

import java.util.List;

/**
 * Data object to return information about Bnk file.
 */
public class BankInfoDto {

    private int year;
    private int fileSize;
    private List<PackedFileInfoDto> packedFiles;

    public int getYear() {
        return year;
    }

    public int getFileSize() {
        return fileSize;
    }

    public List<PackedFileInfoDto> getPackedFiles() {
        return packedFiles;
    }
}