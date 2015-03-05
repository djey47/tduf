package fr.tduf.libunlimited.high.files.banks;

import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import fr.tduf.libunlimited.low.files.banks.dto.PackedFileInfoDto;

/**
 * Bnk support, default implementation doing nothing.
 */
public class FakeBnkGateway implements BankSupport {

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
    public void extractAll(String bankFileName, String outputDirectory) { }

    @Override
    public void packAll(String inputDirectory, String outputBankFileName) { }
}