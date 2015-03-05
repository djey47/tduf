package fr.tduf.libunlimited.high.files.banks.interop;

import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.banks.dto.PackedFileInfoDto;
import net.sf.jni4net.Bridge;

import java.io.File;

/**
 * Bnk support, implementation relying on TDUMT .net assemblies.
 */
public class GenuineBnkGateway implements BankSupport {

    static {
        try {
            Bridge.init();
            // TODO Generate Assembly with CSC on Windows platform
            Bridge.LoadAndRegisterAssemblyFrom(new File("libs/TduModdingLibrary.j4n.dll"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public BankInfoDto getBankInfo(String bankFileName) {
        return BankInfoDto.builder()
                .fromYear(2015)
                .withFileSize(1500)
                .addPackedFile(PackedFileInfoDto.builder()
                        .forReference("REF")
                        .withSize(500)
                        .withFullName("/euro/bnk/empty.bnk")
                        .build())
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
