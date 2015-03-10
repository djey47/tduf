package fr.tduf.libunlimited.high.files.banks.interop;

import org.junit.Test;

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

    }

    @Test
    public void getFileNameFromPathCompounds() throws Exception {

    }
}