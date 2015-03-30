package fr.tduf.libunlimited.high.files.banks.interop;

import fr.tduf.libunlimited.high.files.banks.interop.domain.ProcessResult;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class GenuineBnkGatewayTest {

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

    @Test(expected = NullPointerException.class)
    public void runCliCommand_whenNullCommand_shouldThrowException() throws IOException {
        // GIVEN-WHEN
        GenuineBnkGateway.runCliCommand(null);

        // THEN: NPE
    }

    @Test(expected = IOException.class)
    public void runCliCommand_whenInvalidCommand_shouldThrowException() throws IOException {
        // GIVEN-WHEN
        GenuineBnkGateway.runCliCommand("aaa");

        // THEN: IOE
    }

    @Test
    public void runCliCommand_whenValidCommand_shouldReturnProcessResult() throws IOException {
        // GIVEN-WHEN
        ProcessResult actualProcessResult = GenuineBnkGateway.runCliCommand("date");
        actualProcessResult.printOut();
        actualProcessResult.printErr();


        // THEN
        assertThat(actualProcessResult).isNotNull();
        assertThat(actualProcessResult.getReturnCode()).isNotEqualTo(-1);

        assertThat(actualProcessResult.getOut()).isNotNull();
        assertThat(actualProcessResult.getErr()).isNotNull();
    }

    @Test
    public void runCliCommand_whenValidCommand_andOneInvalidArgument_shouldReturnProcessInstance() throws IOException {
        // GIVEN-WHEN
        ProcessResult actualProcessResult = GenuineBnkGateway.runCliCommand("sort", "/JUKE");
        actualProcessResult.printOut();
        actualProcessResult.printErr();

        // THEN
        assertThat(actualProcessResult).isNotNull();
    }
}