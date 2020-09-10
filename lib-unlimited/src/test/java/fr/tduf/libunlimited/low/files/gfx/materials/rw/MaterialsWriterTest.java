package fr.tduf.libunlimited.low.files.gfx.materials.rw;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CamerasDatabase;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasWriter;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import fr.tduf.libunlimited.low.files.research.rw.JsonAdapter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialsWriterTest {
    @Test
    void load_shouldReturnWriterInstance() throws IOException {
        // GIVEN
        MaterialDefs materialDefs = MaterialDefs.builder().build();

        // WHEN
        MaterialsWriter actualWriter = MaterialsWriter.load(materialDefs);

        // THEN
        assertThat(actualWriter).isNotNull();
    }

    @Test
    void write_whenNoChanges_shouldReturnOriginalContentsBack() throws IOException {
        // To regen json input file:
        // FileTool jsonify -i [..]/tduf/lib-testing/src/main/resources/materials/car-sample.2DM -s [..]/tduf/lib-unlimited/src/main/resources/files/structures/2DM-map.json

        // GIVEN
        String materialsContentsFromJSONFile = FilesHelper.readTextFromResourceFile("/materials/car-sample.2DM.json");
        MaterialsParser materialsParser = MaterialsParser.load(new XByteArrayInputStream(new byte[0]));
        new JsonAdapter(materialsParser.getDataStore()).fromJsonString(materialsContentsFromJSONFile);
        MaterialDefs materialDefs = materialsParser.generate();

        // WHEN
        MaterialsWriter actualWriter = MaterialsWriter.load(materialDefs);
        ByteArrayOutputStream actualOutputStream = actualWriter.write();

        // THEN
        byte[] expectedMaterialContents = FilesHelper.readBytesFromResourceFile("/materials/car-sample.2DM");
        assertThat(actualOutputStream).isNotNull();
        assertThat(actualOutputStream.toByteArray()).isEqualTo(expectedMaterialContents);
    }
}
