package fr.tduf.libunlimited.high.files.banks.interop;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.libtesting.common.helper.TestingFilesHelper;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.system.domain.ProcessResult;
import fr.tduf.libunlimited.high.files.banks.interop.dto.GenuineBatchInputDto;
import fr.tduf.libunlimited.low.files.banks.dto.BankInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.PREFIX_ORIGINAL_BANK_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class GenuineBnkGatewayTest {

    private static final String PACKED_FILE_FULL_NAME = "D:\\Eden-Prog\\Games\\TestDrive\\Resources\\4Build\\PC\\EURO\\Vehicules\\Cars\\Mercedes\\CLK_55\\.2DM\\CLK_55";

    @Mock
    private CommandLineHelper commandLineHelperMock;

    @InjectMocks
    private GenuineBnkGateway genuineBnkGateway;

    @Captor
    private ArgumentCaptor<String> commandArgumentsCaptor;

    private String bankFileName;

    private String tempDirectory;


    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        initMocks(this);
        
        tempDirectory = createTempDirectory();

        bankFileName = FilesHelper.getFileNameFromResourcePath("/banks/Vehicules/A3_V6.bnk");

//        Log.set(Log.LEVEL_DEBUG);
    }

    @Test
    void getBankInfo_whenSuccess_shouldInvokeCommandLineCorrectly_andReturnObject() throws IOException {
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

    @Test
    void getBankInfo_whenSystemFailure_shouldInvokeCommandLineCorrectly_andThrowException() throws IOException {
        // GIVEN
        when(commandLineHelperMock.runCliCommand(eq("mono"), anyString(), eq("BANK-I"), eq(bankFileName))).thenThrow(new IOException());

        // WHEN-THEN
        assertThrows(IOException.class,
                () -> genuineBnkGateway.getBankInfo(bankFileName));
    }

    @Test
    void getBankInfo_whenCommandFailure_shouldInvokeCommandLineCorrectly_andThrowException() throws IOException {
        // GIVEN
        mockCommandLineHelperToReturnBankInformationFailure(bankFileName);

        // WHEN-THEN
        assertThrows(IOException.class,
                () -> genuineBnkGateway.getBankInfo(bankFileName));
    }

    @Test
    void extractAll_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException {
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

        verify(commandLineHelperMock, times(1)).runCliCommand(eq("mono"), anyString(), eq("BANK-UX"), eq(bankFileName), commandArgumentsCaptor.capture());

        assertBatchInputFileExists();
    }

    @Test
    void packAll_whenSuccess_shouldInvokeCommandLineCorrectly() throws IOException {
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

        verify(commandLineHelperMock, times(1)).runCliCommand(eq("mono"), anyString(), eq("BANK-RX"), eq(outputBankFileName),  commandArgumentsCaptor.capture());

        assertBatchInputFileExists();
    }

    @Test
    void getInternalPathFromRealPath() {
        // GIVEN
        Path realFilePath = Paths.get("/home/bill/work/4Build/PC/EURO/Vehicules/Cars/Mercedes/CLK_55/CLK_55.2DM");
        Path basePath = Paths.get("/home/bill/work");

        // WHEN
        String actualPackedFilePath = GenuineBnkGateway.getInternalPathFromRealPath(realFilePath, basePath);

        // THEN
        assertThat(actualPackedFilePath).isEqualTo(PACKED_FILE_FULL_NAME);
    }

    @Test
    void getRealFilePathFromInternalPath() {
        // GIVEN
        Path basePath = Paths.get("/home/bill/work/");

        // WHEN
        Path actualRealPath = GenuineBnkGateway.getRealFilePathFromInternalPath(PACKED_FILE_FULL_NAME, basePath);

        // THEN
        assertThat(actualRealPath).isEqualTo(Paths.get("/home/bill/work/4Build/PC/EURO/Vehicules/Cars/Mercedes/CLK_55/CLK_55.2DM"));
    }

    @Test
    void generatePackedFileReference() {
        // GIVEN-WHEN
        String actualReference = GenuineBnkGateway.generatePackedFileReference(PACKED_FILE_FULL_NAME);

        // THEN
        assertThat(actualReference).isEqualTo("3367621430");
    }

    @Test
    void searchOriginalBankFilePath_whenNoFilePresent_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IOException.class,
                () -> GenuineBnkGateway.searchOriginalBankPath(tempDirectory));
    }

    @Test
    void searchOriginalBankFilePath_whenCorrectFilePresent_shouldReturnFileName() throws IOException {
        // GIVEN
        String officialBankFileName = "A3_V6.bnk";
        createRepackedFileTree(officialBankFileName);

        // WHEN
        Path actualFilePath = GenuineBnkGateway.searchOriginalBankPath(tempDirectory);

        // THEN
        assertThat(actualFilePath.getFileName().toString()).isEqualTo("original-" + officialBankFileName);
    }

    private static String createTempDirectory() throws IOException {
        return TestingFilesHelper.createTempDirectoryForLibrary();
    }

    private void createRepackedFileTree(String bankFileName) throws IOException {

        Path contentsPath = Paths.get(tempDirectory, "4Build", "PC", "EURO", "Vehicules", "Cars", "Audi", "A3_V6");
        Files.createDirectories(contentsPath);
        Files.createFile(Paths.get(tempDirectory, PREFIX_ORIGINAL_BANK_FILE + bankFileName));

        Files.createFile(contentsPath.resolve("A3_V6.3DD"));
        Files.createFile(contentsPath.resolve("A3_V6.3DG"));
        Files.createFile(contentsPath.resolve("A3_V6.2DM"));
    }

    private void mockCommandLineHelperToReturnBankInformationSuccess(String bankFileName) throws IOException {
        String jsonOutput = FilesHelper.readTextFromResourceFile("/files/interop/tdumt-cli/BANK-I.output.json");
        ProcessResult processResult = new ProcessResult("BANK-I", 0, jsonOutput, "");
        when(commandLineHelperMock.runCliCommand(eq("mono"), anyString(), eq("BANK-I"), eq(bankFileName))).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnBankInformationFailure(String bankFileName) throws IOException {
        ProcessResult processResult = new ProcessResult("BANK-I", 1, "", "Failure!");
        when(commandLineHelperMock.runCliCommand(eq("mono"), anyString(), eq("BANK-I"), eq(bankFileName))).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnExtractionSuccess(String bankFileName) throws IOException {
        ProcessResult processResult = new ProcessResult("BANK-UX", 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq("mono"), anyString(), eq("BANK-UX"), eq(bankFileName), anyString())).thenReturn(processResult);
    }

    private void mockCommandLineHelperToReturnReplaceSuccess(String bankFileName) throws IOException {
        ProcessResult processResult = new ProcessResult("BANK-RX", 0, "{}", "");
        when(commandLineHelperMock.runCliCommand(eq("mono"), anyString(), eq("BANK-RX"), eq(bankFileName), anyString())).thenReturn(processResult);
    }

    private void assertBatchInputFileExists() throws IOException {
        final Path batchInputPath = Paths.get(commandArgumentsCaptor.getValue());
        assertThat(batchInputPath).exists();

        Log.info("Batch input file name: " + batchInputPath);

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
