package fr.tduf.libunlimited.high.files.banks.interop;

import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.banks.dto.PackedFileInfoDto;
import net.sf.jni4net.Bridge;
import tdumoddinglibrary.fileformats.TduFile;
import tdumoddinglibrary.fileformats.banks.BNK;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Bnk support, implementation relying on TDUMT .net assemblies.
 */
public class GenuineBnkGateway implements BankSupport {

    static {
        try {
            Bridge.init();
            // TODO Generate Assemblies with CSC on Windows platform
            Bridge.LoadAndRegisterAssemblyFrom(new File("libs/TduModdingLibrary.j4n.dll"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public BankInfoDto getBankInfo(String bankFileName) {

        BNK bankFile = (BNK) TduFile.GetFile(bankFileName);

        List<PackedFileInfoDto> packedFilesInfos = new ArrayList<>();

        for (int i = 0 ; i < bankFile.getPackedFilesCount() ; i++) {
            PackedFileInfoDto packedFileInfo = PackedFileInfoDto.builder()
                        .forReference("REF")
                        .withSize(500)
                        .withFullName("/euro/bnk/empty.bnk")
                        .build();

            packedFilesInfos.add(packedFileInfo);
        }

        return BankInfoDto.builder()
                .fromYear(bankFile.getYear())
                .withFileSize(bankFile.getSize())
                .addPackedFiles(packedFilesInfos)
                .build();
    }

    @Override
    public void extractAll(String bankFileName, String outputDirectory) {
        // TODO make copy of BNK to outputDirectory (will make future repacking easier)
    }

    @Override
    public void packAll(String inputDirectory, String outputBankFileName) {

    }
}
