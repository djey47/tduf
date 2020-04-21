package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CamerasDatabase;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.List;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CamerasParserTest {

    private static byte[] camContents;

    @BeforeAll
    static void setUp() throws IOException {
        camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
    }

    @Test
    void parse_whenRealFiles_shouldLoadCamerasContents_andFillCaches() throws IOException {
        // GIVEN
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);

        // WHEN
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        CamerasDatabase parsedContents = camerasParser.parse();

        // THEN
        assertThat(parsedContents.getIndexSize()).isEqualTo(150);
        assertThat(parsedContents.getSetsCount()).isEqualTo(148);
        assertThat(parsedContents.getTotalViewCount()).isEqualTo(591);
    }

    @Test
    void getViewProps_whenNullDataStore_shouldThrowException() throws IOException {
        // GIVEN
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        camerasParser.parse();

        // WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> camerasParser.getViewProps(null));
    }

    @Test
    void getViewProps_whenViewDataStore_shouldReturnProperties() throws IOException {
        // GIVEN
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        camerasParser.parse();
        DataStore dataStore = new DataStore(FileStructureDto.builder().build());
        dataStore.addInteger32("type", 45L);
        dataStore.addInteger32("steeringWheelTurn", 30L);
        dataStore.addInteger32("binoculars", 0L);
        dataStore.addInteger32("cameraPositionX", -100L);
        dataStore.addInteger32("viewPositionZ", 100L);

        // WHEN
        EnumMap<ViewProps, ?> viewProps = camerasParser.getViewProps(dataStore);

        // THEN
        assertThat(viewProps).hasSize(4);
        assertThat(viewProps.get(ViewProps.STEERING_WHEEL_TURN)).isEqualTo(30L);
        assertThat(viewProps.get(ViewProps.BINOCULARS)).isEqualTo(0L);
        assertThat(viewProps.get(ViewProps.CAMERA_POSITION_X)).isEqualTo(-100L);
        assertThat(viewProps.get(ViewProps.VIEW_POSITION_Z)).isEqualTo(100L);
    }

    @Test
    void generate_whenRealFiles_shouldReturnDomainObject() throws IOException {
        // GIVEN
        CamerasParser camerasParser = CamerasParser.load(new ByteArrayInputStream(camContents));
        camerasParser.parse();

        // WHEN
        CamerasDatabase actualInfo = camerasParser.generate();

        // THEN
        assertThat(actualInfo).isNotNull();
        assertThat(actualInfo.getIndexSize()).isEqualTo(150);
        assertThat(actualInfo.getSetsCount()).isEqualTo(148);

        List<CameraView> views = actualInfo.getViewsForCameraSet(1);
        assertThat(views).hasSize(4);
        assertThat(views).extracting("kind").contains(Cockpit, Hood, Cockpit_Back, Hood_Back);
        assertThat(views).extracting("cameraSetId").containsOnly(1);

        assertThat(actualInfo.getTotalViewCount()).isEqualTo(591);
    }
}
