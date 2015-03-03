package fr.tduf.libunlimited.low.files.banks.dto;

/**
 * Data object to return information about packed file (Bank contents).
 */
public class PackedFileInfoDto {

    private String reference;
    private String fullName;
    private int size;

    public String getReference() {
        return reference;
    }

    public String getFullName() {
        return fullName;
    }

    public int getSize() {
        return size;
    }
}
