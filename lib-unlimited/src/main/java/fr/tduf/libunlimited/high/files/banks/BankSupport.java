package fr.tduf.libunlimited.high.files.banks;

import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;

import java.io.IOException;

/**
 * Contract for handling of BNK files. Allows to provide many ways of BNK management
 * (Genuine/Interop, native, mocked ...)
 */
public interface BankSupport {

    /**
     * @param bankFileName  : location of bank file to fetch information from
     * @return misc. information about provided file name.
     */
    BankInfoDto getBankInfo(String bankFileName);

    /**
     * @param bankFileName      : location of bank file to extract contents (all packed files) from
     * @param outputDirectory   : location to place extracted files. Must exist.
     */
    void extractAll(String bankFileName, String outputDirectory) throws IOException;

    /**
     * @param inputDirectory        : location to extracted files to pack into bank
     * @param outputBankFileName    : location of bank file to create.
     */
    void packAll(String inputDirectory, String outputBankFileName);
}