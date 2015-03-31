package fr.tduf.libunlimited.high.files.banks.interop;

import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.interop.domain.ProcessResult;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.CLI_COMMAND_BANK_INFO;
import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.EXE_TDUMT_CLI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenuineBnkGatewayTest {

    @Mock
    CommandLineHelper commandLineHelperMock;

    @InjectMocks
    private GenuineBnkGateway genuineBnkGateway;

    private final String bankFileName = "C:\\File.bnk";

    @Test
    public void getBankInfo_whenSuccess_shouldInvokeCommandLineCorrectly_andReturnObject() throws IOException, URISyntaxException {
        // GIVEN
        String jsonOutput = FilesHelper.readTextFromResourceFile("/files/interop/tdumt-cli/BANK-I.output.json");
        ProcessResult processResult = new ProcessResult(CLI_COMMAND_BANK_INFO, 0, jsonOutput, "");
        when(commandLineHelperMock.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_INFO, bankFileName)).thenReturn(processResult);


        // WHEN
        BankInfoDto actualBankInfoObject = genuineBnkGateway.getBankInfo(bankFileName);


        // THEN
        assertThat(actualBankInfoObject).isNotNull();
        assertThat(actualBankInfoObject.getFileSize()).isEqualTo(2947448);
        assertThat(actualBankInfoObject.getYear()).isEqualTo(2015);
        assertThat(actualBankInfoObject.getPackedFiles()).hasSize(28);
    }

    @Test(expected = IOException.class)
    public void getBankInfo_whenFailure_shouldInvokeCommandLineCorrectly_andThrowException() throws IOException, URISyntaxException {
        // GIVEN
        when(commandLineHelperMock.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_INFO, bankFileName)).thenThrow(new IOException());

        // WHEN
        genuineBnkGateway.getBankInfo(bankFileName);

        // THEN: IOException
    }

    @Test
    @Ignore
    public void extractAll_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException {
        // GIVEN
        String outputDirectory = "C:\\File";

        // WHEN
        genuineBnkGateway.extractAll(bankFileName, outputDirectory);

        // THEN
    }

    @Test
    @Ignore
    public void packAll_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException {
        // GIVEN
        String inputDirectory = "C:\\File";

        // WHEN
        genuineBnkGateway.packAll(inputDirectory, bankFileName);

        // THEN
    }

    @Test
    public void getInternalPackedFilePath() throws Exception {
        // GIVEN
        Path packedFilePath = Paths.get("/home/bill/work/4Build/PC/EURO/Vehicules/Cars/Mercedes/CLK_55/CLK_55.2DM");
        Path basePath = Paths.get("/home/bill/work");

        // WHEN
        String actualPackedFilePath = GenuineBnkGateway.getInternalPackedFilePath(packedFilePath, basePath);

        // THEN
        assertThat(actualPackedFilePath).isEqualTo("\\D:\\Eden-Prog\\Games\\TestDrive\\Resources\\4Build\\PC\\EURO\\Vehicules\\Cars\\Mercedes\\CLK_55\\.2DM\\CLK_55");
    }

    @Test
    public void getTargetFileNameFromPathCompounds() throws Exception {
        // GIVEN
        String bankFileName = "/home/bill/work/File.bnk";
        String[] filePathCompounds = new String[]{"", "D:", "Eden-Prog", "Games", "TestDrive", "Resources", "4Build", "PC", "EURO", "Vehicules", "Cars", "Mercedes", "CLK_55", ".2DM", "CLK_55"};

        // WHEN
        String actualFileName = GenuineBnkGateway.getTargetFileNameFromPathCompounds(bankFileName, filePathCompounds);

        // THEN
        assertThat(actualFileName.replace('\\', '/')).isEqualTo("/home/bill/work/File.bnk/4Build/PC/EURO/Vehicules/Cars/Mercedes/CLK_55/CLK_55.2DM");
    }

    @Test
    public void getFileNameFromPathCompounds() throws Exception {
        // GIVEN
        String[] filePathCompounds = new String[]{"", "D:", "Eden-Prog", "Games", "TestDrive", "Resources", "4Build", "PC", "EURO", "Vehicules", "Cars", "Mercedes", "CLK_55", ".2DM", "CLK_55"};

        // WHEN
        String actualFileName = GenuineBnkGateway.getFileNameFromPathCompounds(filePathCompounds);

        // THEN
        assertThat(actualFileName).isEqualTo("CLK_55.2DM");
    }

    @Test
    public void generatePackedFileReference() {
        // GIVEN
        String packedFilePath = "\\D:\\Eden-Prog\\Games\\TestDrive\\Resources\\4Build\\PC\\EURO\\Vehicules\\Cars\\Mercedes\\CLK_55\\.2DM\\CLK_55";

        // WHEN
        String actualReference = GenuineBnkGateway.generatePackedFileReference(packedFilePath);

        // THEN
        assertThat(actualReference).isEqualTo("2732794586");
    }
}