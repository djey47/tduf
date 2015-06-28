package fr.tduf.libunlimited.high.files.banks;

import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Contract for handling of BNK files. Allows to provide many ways of BNK management
 * (Genuine/Interop, native, mocked ...)
 */
public interface BankSupport {

    /**
     * @param bankFileName  : location of bank file to fetch information from
     * @return misc. information about provided file name.
     */
    BankInfoDto getBankInfo(String bankFileName) throws IOException;

    /**
     * @param bankFileName      : location of bank file to extract contents (all packed files) from
     * @param outputDirectory   : location to place extracted files. Must exist.
     */
    void extractAll(String bankFileName, String outputDirectory) throws IOException;

    /**
     * Requirements:
     * - input directory must have official name of BNK file
     * - input directory must contain:
     *      -> original-[official bank name].bnk file
     *      -> packed file hierarchy
     * @param inputDirectory        : location to extracted files to pack into bank
     * @param outputBankFileName    : location of bank file to create.
     */
    void packAll(String inputDirectory, String outputBankFileName) throws IOException;

    /**
     * Implement and use this method when a particular file layout is required by packing operation.
     * @param sourceDirectory       : directory as source for repacked files
     * @param repackedPaths         : list of paths of files to be repacked
     * @param targetBankFileName    : name of bank file to be created
     * @param targetDirectory       : directory to place new file layout
     * @throws IOException
     */
     void prepareFilesToBeRepacked(String sourceDirectory, List<Path> repackedPaths, String targetBankFileName, String targetDirectory) throws IOException;
}