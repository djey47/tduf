package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CamerasParserTest {

    private static byte[] camContents;

    @BeforeAll
    static void setUp() throws IOException, URISyntaxException {
        camContents = FilesHelper.readBytesFromResourceFile("/bin/Cameras.bin");
    }

    @Test
    void parse_whenRealFiles_shouldLoadCamerasContents_andFillCaches() throws URISyntaxException, IOException {
        // GIVEN
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);


        // WHEN
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        camerasParser.parse();


        // THEN
        assertThat(camerasParser).isNotNull();
        assertThat(camerasParser.getCameraIndex()).hasSize(150);
        assertThat(camerasParser.getCameraViews()).hasSize(148);
        assertThat(camerasParser.getTotalViewCount()).isEqualTo(591);

        assertThat(camerasParser.getCachedCameraIndex()).isNotEmpty();
        assertThat(camerasParser.getCachedCameraViews()).isNotEmpty();
        assertThat(camerasParser.getCachedTotalViewCount()).isNotNull();
    }

    @Test
    void flushCaches_shouldNullifyAllCaches() throws IOException, URISyntaxException {
        // GIVEN
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        camerasParser.parse();
        camerasParser.getCameraViews();
        camerasParser.getCameraIndex();

        // WHEN
        camerasParser.flushCaches();

        // THEN
        assertThat(camerasParser.getCachedCameraViews()).isNull();
        assertThat(camerasParser.getCachedCameraIndex()).isNull();
        assertThat(camerasParser.getCachedTotalViewCount()).isNull();
    }

    @Test
    void getViewProps_whenIncorrectDataStore_shouldThrowException() throws IOException {
        // GIVEN
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        camerasParser.parse();
        DataStore dataStore = new DataStore(FileStructureDto.builder().build());

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> camerasParser.getViewProps(dataStore));
    }

    @Test
    void getViewProps_whenViewDataStore_shouldReturnProperties() throws IOException {
        // GIVEN
        ByteArrayInputStream camInputStream = new ByteArrayInputStream(camContents);
        CamerasParser camerasParser = CamerasParser.load(camInputStream);
        camerasParser.parse();
        DataStore dataStore = new DataStore(FileStructureDto.builder().build());
        dataStore.addInteger("type", 45L);
        dataStore.addInteger("steeringWheelTurn", 30L);
        dataStore.addInteger("binoculars", 0L);
        dataStore.addInteger("cameraPositionX", -100L);
        dataStore.addInteger("viewPositionZ", 100L);

        // WHEN
        EnumMap<ViewProps, ?> viewProps = camerasParser.getViewProps(dataStore);

        // THEN
        assertThat(viewProps).hasSize(5);
        assertThat(viewProps.get(ViewProps.TYPE)).isEqualTo(ViewKind.Follow_Large_Back);
        assertThat(viewProps.get(ViewProps.STEERING_WHEEL_TURN)).isEqualTo(30L);
        assertThat(viewProps.get(ViewProps.BINOCULARS)).isEqualTo(0L);
        assertThat(viewProps.get(ViewProps.CAMERA_POSITION_X)).isEqualTo(-100L);
        assertThat(viewProps.get(ViewProps.VIEW_POSITION_Z)).isEqualTo(100L);
    }
}
