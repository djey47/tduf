package fr.tduf.libunlimited.low.files.gfx.materials.rw;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialsParserTest {
    private static byte[] matColorsContents;
    private static byte[] matCarContents;

    @BeforeAll
    static void setUp() throws IOException {
        matColorsContents = FilesHelper.readBytesFromResourceFile("/materials/colors-sample.2DM");
        matCarContents = FilesHelper.readBytesFromResourceFile("/materials/car-sample.2DM");

        Log.set(Log.LEVEL_INFO);
    }

    @Test
    void generate_whenColorsFile_shouldReturnDomainObject() throws IOException {
        // GIVEN
        MaterialsParser materialsParser = MaterialsParser.load(new XByteArrayInputStream(matColorsContents));
        materialsParser.parse();

        // WHEN
        MaterialDefs actualDefs = materialsParser.generate();

        // THEN
        assertThat(actualDefs).isNotNull();
        assertThat(actualDefs.getMaterials()).hasSize(523);
        Material firstMaterial = actualDefs.getMaterials().get(0);
        assertThat(firstMaterial.getName()).isEqualTo("B_10");
        assertThat(firstMaterial.getProperties()).isNotNull();
    }

    @Test
    void generate_whenCarFile_shouldReturnDomainObject() throws IOException {
        // GIVEN
        MaterialsParser materialsParser = MaterialsParser.load(new XByteArrayInputStream(matCarContents));
        materialsParser.parse();

        // WHEN
        MaterialDefs actualDefs = materialsParser.generate();

        // THEN
        assertThat(actualDefs).isNotNull();
        assertThat(actualDefs.getMaterials()).hasSize(36);
        Material firstMaterial = actualDefs.getMaterials().get(0);
        assertThat(firstMaterial.getName()).isEqualTo("FRL_F");
        assertThat(firstMaterial.getProperties()).isNotNull();
    }
}
