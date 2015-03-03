package fr.tduf.libunlimited.high.files.banks.interop;

import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;

public class GenuineBnkGateway implements BankSupport {
    @Override
    public BankInfoDto getBankInfo(String bankFileName) {
        return null;
    }

    @Override
    public void extractAll(String bankFileName, String outputDirectory) {

    }

    @Override
    public void packAll(String inputDirectory, String outputBankFileName) {

    }
}
