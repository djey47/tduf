package fr.tduf.libunlimited.high.files.banks.interop;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.domain.ProcessResult;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.interop.dto.GenuineBatchInputDto;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;

import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.*;
import static java.util.Arrays.asList;
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

    @Captor
    private ArgumentCaptor<String> commandArgumentsCaptor;

    private String bankFileName;

    private String tempDirectory;


    @Before
    public void setUp() throws URISyntaxException, IOException {
        tempDirectory = createTempDirectory();

        bankFileName = FilesHelper.getFileNameFromResourcePath("/banks/Vehicules/A3_V6.bnk");

//        Log.set(Log.LEVEL_DEBUG);
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
        Log.info("Directory for unpacked contents: " + tempDirectory);

        assertThat(Paths.get(tempDirectory, PREFIX_ORIGINAL_BANK_FILE + Paths.get(bankFileName).getFileName())).exists();

        Path targetParentPath = Paths.get(tempDirectory, "4Build", "PC", "EURO", "Vehicules", "Cars", "Mercedes", "CLK_55");
        assertThat(targetParentPath).exists();

        verify(commandLineHelperMock, times(1)).runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_BATCH_UNPACK), eq(bankFileName), commandArgumentsCaptor.capture());

        assertBatchInputFileExists();
    }

    @Test
    public void packAll_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException, URISyntaxException {
        // GIVEN
        String bankShortName = "CLK_55.bnk";
        createRepackedFileTree(bankShortName);

        String originalBankFileName = Paths.get(tempDirectory, "original-" + bankShortName).toString();
        String outputBankFileName = Paths.get(tempDirectory, "repacked-" + bankShortName).toString();
        mockCommandLineHelperToReturnBankInformationSuccess(originalBankFileName);
        mockCommandLineHelperToReturnReplaceSuccess(outputBankFileName);


        // WHEN
        genuineBnkGateway.packAll(Paths.get(tempDirectory).toString(), outputBankFileName);


        // THEN
        Log.info("Directory for repacked contents: " + tempDirectory);

        assertThat(new File(outputBankFileName)).exists();

        verify(commandLineHelperMock, times(1)).runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_BATCH_REPLACE), eq(outputBankFileName),  commandArgumentsCaptor.capture());

        assertBatchInputFileExists();
    }

    @Test
    public void getInternalPathFromRealPath() throws Exception {
        // GIVEN
        Path realFilePath = Paths.get("/home/bill/work/4Build/PC/EURO/Vehicules/Cars/Mercedes/CLK_55/CLK_55.2DM");
        Path basePath = Paths.get("/home/bill/work");

        // WHEN
        String actualPackedFilePath = GenuineBnkGateway.getInternalPathFromRealPath(realFilePath, basePath);

        // THEN
        assertThat(actualPackedFilePath).isEqualTo(PACKED_FILE_FULL_NAME);
    }

    @Test
    public void getRealFilePathFromInternalPath() {
        // GIVEN
        Path basePath = Paths.get("/home/bill/work/");

        // WHEN
        Path actualRealPath = GenuineBnkGateway.getRealFilePathFromInternalPath(PACKED_FILE_FULL_NAME, basePath);

        // THEN
        assertThat(actualRealPath).isEqualTo(Paths.get("/home/bill/work/4Build/PC/EURO/Vehicules/Cars/Mercedes/CLK_55/CLK_55.2DM"));
    }

    @Test
    public void generatePackedFileReference() {
        // GIVEN-WHEN
        String actualReference = GenuineBnkGateway.generatePackedFileReference(PACKED_FILE_FULL_NAME);

        // THEN
        assertThat(actualReference).isEqualTo("3367621430");
    }

    @Test(expected = NoSuchElementException.class)
    public void searchOriginalBankFilePath_whenNoFilePresent_shouldThrowException() throws IOException {
        // GIVEN-WHEN
        GenuineBnkGateway.searchOriginalBankPath(tempDirectory);

        // THEN: exception
    }

    @Test
    public void searchOriginalBankFilePath_whenCorrectFilePresent_shouldReturnFileName() throws IOException {
        // GIVEN
        String officialBankFileName = "A3_V6.bnk";
        createRepackedFileTree(officialBankFileName);

        // WHEN
        Path actualFilePath = GenuineBnkGateway.searchOriginalBankPath(tempDirectory);

        // THEN
        assertThat(actualFilePath.getFileName().toString()).isEqualTo("original-" + officialBankFileName);
    }

    private static String createTempDirectory() throws IOException {
        return fr.tduf.libtesting.common.helper.FilesHelper.createTempDirectoryForLibrary();
    }

    private List<Path> createRepackedFileTree(String bankFileName) throws IOException {

        Path contentsPath = Paths.get(tempDirectory, "4Build", "PC", "EURO", "Vehicules", "Cars", "Audi", "A3_V6");
        Files.createDirectories(contentsPath);
        Files.createFile(Paths.get(tempDirectory, PREFIX_ORIGINAL_BANK_FILE + bankFileName));

        Path filePath1 = Files.createFile(contentsPath.resolve("A3_V6.3DD"));
        Path filePath2 = Files.createFile(contentsPath.resolve("A3_V6.3DG"));
        Path filePath3 = Files.createFile(contentsPath.resolve("A3_V6.2DM"));

        return asList(filePath1, filePath2, filePath3);
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
        ProcessResult processResult = new ProcessResult(CLI_COMMAND_BANK_BATCH_UNPACK, 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_BATCH_UNPACK), eq(bankFileName), anyString())).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnReplaceSuccess(String bankFileName) throws IOException {
        ProcessResult processResult = new ProcessResult(CLI_COMMAND_BANK_BATCH_REPLACE, 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq(EXE_TDUMT_CLI), eq(CLI_COMMAND_BANK_BATCH_REPLACE), eq(bankFileName), anyString())).thenReturn(processResult);
    }

    private void assertBatchInputFileExists() throws IOException {
        String batchInputFileName = commandArgumentsCaptor.getAllValues().get(2);
        final Path batchInputPath = Paths.get(batchInputFileName);
        assertThat(batchInputPath).exists();

        Log.info("Batch input file name: " + batchInputFileName);

        final GenuineBatchInputDto actualBatchInput = new ObjectMapper().readValue(batchInputPath.toFile(), GenuineBatchInputDto.class);
        assertThat(actualBatchInput.getItems()).hasSize(28);

        actualBatchInput.getItems()
                .forEach((item) -> {
                    assertThat(item.getExternalFile())
                            .isNotNull()
                            .isNotEmpty();
                    assertThat(item.getInternalPath())
                            .isNotNull()
                            .isNotEmpty();
                });

    }
}
