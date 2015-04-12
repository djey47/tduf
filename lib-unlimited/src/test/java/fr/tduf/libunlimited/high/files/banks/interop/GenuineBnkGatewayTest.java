package fr.tduf.libunlimited.high.files.banks.interop;

import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.domain.ProcessResult;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GenuineBnkGatewayTest {

    private static final String PACKED_FILE_FULL_NAME = "D:\\Eden-Prog\\Games\\TestDrive\\Resources\\4Build\\PC\\EURO\\Vehicules\\Cars\\Mercedes\\CLK_55\\.2DM\\CLK_55";

    @Mock
    private CommandLineHelper commandLineHelperMock;

    @InjectMocks
    private GenuineBnkGateway genuineBnkGateway;

    private String bankFileName;

    private String tempDirectory;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        tempDirectory = Files.createTempDirectory("libUnlimited-tests").toString();

        bankFileName = FilesHelper.getFileNameFromResourcePath("/banks/Vehicules/A3_V6.bnk");
    }

    @Test
    public void getBankInfo_whenSuccess_shouldInvokeCommandLineCorrectly_andReturnObject() throws IOException, URISyntaxException {
        // GIVEN
        mockCommandLineHelperToReturnBankInformationSuccess(bankFileName);

        // WHEN
        BankInfoDto actualBankInfoObject = genuineBnkGateway.getBankInfo(bankFileName);

        // THEN
        assertThat(actualBankInfoObject).isNotNull();
        assertThat(actualBankInfoObject.getFileSize()).isEqualTo(2947448);
        assertThat(actualBankInfoObject.getYear()).isEqualTo(2015);
        assertThat(actualBankInfoObject.getPackedFiles()).hasSize(28);
    }

    @Test(expected = IOException.class)
    public void getBankInfo_whenSystemFailure_shouldInvokeCommandLineCorrectly_andThrowException() throws IOException, URISyntaxException {
        // GIVEN
        when(commandLineHelperMock.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_INFO, bankFileName)).thenThrow(new IOException());

        // WHEN
        genuineBnkGateway.getBankInfo(bankFileName);

        // THEN: IOException
    }

    @Test(expected = IOException.class)
    public void getBankInfo_whenCommandFailure_shouldInvokeCommandLineCorrectly_andThrowException() throws IOException, URISyntaxException {
        // GIVEN
        mockCommandLineHelperToReturnBankInformationFailure(bankFileName);

        // WHEN
        genuineBnkGateway.getBankInfo(bankFileName);

        // THEN: IOException
    }

    @Test
    public void extractAll_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException, URISyntaxException {
        // GIVEN
        mockCommandLineHelperToReturnBankInformationSuccess(bankFileName);
        mockCommandLineHelperToReturnExtractionSuccess(bankFileName);


        // WHEN
        genuineBnkGateway.extractAll(bankFileName, tempDirectory);


        // THEN
        assertThat(new File(tempDirectory, ORIGINAL_BANK_NAME)).exists();

        String shortBankFileName = Paths.get(bankFileName).getFileName().toString();
        assertThat(Files.exists(Paths.get(tempDirectory, shortBankFileName, "4Build", "PC", "Euro", "Vehicules", "Cars", "Mercedes", "CLK_55")));

        verify(commandLineHelperMock, times(28)).runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_UNPACK), eq(bankFileName), anyString(), eq(tempDirectory));
        verify(commandLineHelperMock).runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_UNPACK), eq(bankFileName), eq("D:\\Eden-Prog\\Games\\TestDrive\\Resources\\4Build\\PC\\EURO\\Vehicules\\Cars\\Mercedes\\CLK_55\\.3DD\\CLK_55"), eq(tempDirectory));
        verify(commandLineHelperMock).runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_UNPACK), eq(bankFileName), eq("D:\\Eden-Prog\\Games\\TestDrive\\Resources\\4Build\\PC\\EURO\\Vehicules\\Cars\\Mercedes\\CLK_55\\.3DG\\CLK_55"), eq(tempDirectory));
        verify(commandLineHelperMock).runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_UNPACK), eq(bankFileName), eq("D:\\Eden-Prog\\Games\\TestDrive\\Resources\\4Build\\PC\\EURO\\Vehicules\\Cars\\Mercedes\\CLK_55\\.2DM\\CLK_55"), eq(tempDirectory));
    }

    @Test
    public void packAll_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException, URISyntaxException {
        // GIVEN
        createSourceFileTree();

        String outputBankFileName = Paths.get(tempDirectory, "A3_V6.output.bnk").toString();
        String sourceDirectory = Paths.get(tempDirectory, "A3_V6.bnk").toString();

        mockCommandLineHelperToReturnBankInformationSuccess(outputBankFileName);
        mockCommandLineHelperToReturnReplaceSuccess(outputBankFileName);


        // WHEN
        genuineBnkGateway.packAll(tempDirectory, outputBankFileName);


        // THEN
        assertThat(new File(outputBankFileName)).exists();

        verify(commandLineHelperMock).runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_REPLACE), eq(outputBankFileName), eq("D:\\Eden-Prog\\Games\\TestDrive\\Resources\\.3DD\\A3_V6"), eq(Paths.get(sourceDirectory, "A3_V6.3DD").toString()));
        verify(commandLineHelperMock).runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_REPLACE), eq(outputBankFileName), eq("D:\\Eden-Prog\\Games\\TestDrive\\Resources\\.3DG\\A3_V6"), eq(Paths.get(sourceDirectory, "A3_V6.3DG").toString()));
        verify(commandLineHelperMock).runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_REPLACE), eq(outputBankFileName), eq("D:\\Eden-Prog\\Games\\TestDrive\\Resources\\.2DM\\A3_V6"), eq(Paths.get(sourceDirectory, "A3_V6.2DM").toString()));
    }

    @Test
    public void getInternalPackedFilePath() throws Exception {
        // GIVEN
        Path packedFilePath = Paths.get("/home/bill/work/4Build/PC/EURO/Vehicules/Cars/Mercedes/CLK_55/CLK_55.2DM");
        Path basePath = Paths.get("/home/bill/work");

        // WHEN
        String actualPackedFilePath = GenuineBnkGateway.getInternalPackedFilePath(packedFilePath, basePath);

        // THEN
        assertThat(actualPackedFilePath).isEqualTo(PACKED_FILE_FULL_NAME);
    }

    @Test
    public void generatePackedFileReference() {
        // GIVEN-WHEN
        String actualReference = GenuineBnkGateway.generatePackedFileReference(PACKED_FILE_FULL_NAME);

        // THEN
        assertThat(actualReference).isEqualTo("3367621430");
    }

    @Test(expected = NoSuchElementException.class)
    public void searchOriginalBankFileName_whenNoDirectoryPresent_shouldThrowException() throws IOException {
        // GIVEN-WHEN
        GenuineBnkGateway.searchOriginalBankFileName(tempDirectory);

        // THEN: exception
    }

    @Test
    public void searchOriginalBankFileName_whenCorrectDirectoryPresent_shouldReturnDirectoryName() throws IOException {
        // GIVEN
        createSourceFileTree();

        // WHEN
        String actualFileName = GenuineBnkGateway.searchOriginalBankFileName(tempDirectory);

        // THEN
        assertThat(actualFileName).isEqualTo("A3_V6.bnk");
    }

    private void createSourceFileTree() throws IOException {
        assert new File(tempDirectory, ORIGINAL_BANK_NAME).createNewFile();

        Path extractedPath = Paths.get(tempDirectory, "A3_V6.bnk");
        Files.createDirectories(extractedPath);
        String extractedDirectory = extractedPath.toString();

        assert new File(extractedDirectory, "A3_V6.3DD").createNewFile();
        assert new File(extractedDirectory, "A3_V6.3DG").createNewFile();
        assert new File(extractedDirectory, "A3_V6.2DM").createNewFile();
    }

    private void mockCommandLineHelperToReturnBankInformationSuccess(String bankFileName) throws URISyntaxException, IOException {
        String jsonOutput = FilesHelper.readTextFromResourceFile("/files/interop/tdumt-cli/BANK-I.output.json");
        ProcessResult processResult = new ProcessResult(CLI_COMMAND_BANK_INFO, 0, jsonOutput, "");
        when(commandLineHelperMock.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_INFO, bankFileName)).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnBankInformationFailure(String bankFileName) throws IOException {
        ProcessResult processResult = new ProcessResult(CLI_COMMAND_BANK_INFO, 1, "", "Failure!");
        when(commandLineHelperMock.runCliCommand(EXE_TDUMT_CLI, CLI_COMMAND_BANK_INFO, bankFileName)).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnExtractionSuccess(String bankFileName) throws IOException {
        ProcessResult processResult = new ProcessResult(CLI_COMMAND_BANK_UNPACK, 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_UNPACK), eq(bankFileName), anyString(), anyString())).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnReplaceSuccess(String bankFileName) throws IOException {
        ProcessResult processResult = new ProcessResult(CLI_COMMAND_BANK_REPLACE, 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_REPLACE), eq(bankFileName), anyString(), anyString())).thenReturn(processResult);
    }
}