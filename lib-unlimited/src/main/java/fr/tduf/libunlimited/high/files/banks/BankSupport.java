package fr.tduf.libunlimited.high.files.banks;

import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;

/**
 * Contract for handling of BNK files. Allows to provide many ways of BNK management
 * (Genuine/Interop, native, mocked ...)
 */
public interface BankSupport {

    /**
     *
     * @param bankFileName
     * @return
     */
    BankInfoDto getBankInfo(String bankFileName);

    /**
     *
     * @param bankFileName
     * @param outputDirectory
     */
    void extractAll(String bankFileName, String outputDirectory);

    /**
     *
     * @param inputDirectory
     * @param outputBankFileName
     */
    void packAll(String inputDirectory, String outputBankFileName);

    // Later ?
//    void extractPackedFileWithReference(String bankFileName, String packedFileRef, String outputDirectory);
//
//    void extractPackedFileWithFullName(String bankFileName, String packedFileName, String outputDirectory);
}