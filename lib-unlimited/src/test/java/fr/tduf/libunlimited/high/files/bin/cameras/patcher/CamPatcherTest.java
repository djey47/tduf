package fr.tduf.libunlimited.high.files.bin.cameras.patcher;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.ViewChangeDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CamerasDatabase;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.VIEW_POSITION_X;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class CamPatcherTest {

    @Mock
    private CamerasDatabase camerasDatabaseMock;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void new_whenNullInfo_shouldThrowException() {
        // given-when-then
        assertThrows(NullPointerException.class,
                () -> new CamPatcher(null));
    }

    @Test
    void apply_whenNullPatchObject_shouldThrowException() {
        // given-when
        CamPatcher camPatcher = new CamPatcher(camerasDatabaseMock);

        // then
        assertThrows(NullPointerException.class,
                () -> camPatcher.apply(null));
    }

    @Test
    void apply_whenCameraSetExists_shouldModifyCameraInfo() {
        // given
        CamPatcher camPatcher = new CamPatcher(camerasDatabaseMock);
        ViewChangeDto viewChangeDto = ViewChangeDto.builder()
                .forViewKind(Cockpit)
                .addProp(VIEW_POSITION_X, "1500")
                .build();
        SetChangeDto setChangeObject = SetChangeDto.builder()
                .withSetIdentifier(125)
                .addChanges(singletonList(viewChangeDto))
                .build();
        CamPatchDto camPatchDto = CamPatchDto.builder().addChanges(singletonList(setChangeObject)).build();

        DataStore dataStoreMock = mock(DataStore.class);
        List<CameraView> currentViews = singletonList(CameraView.builder().ofKind(Cockpit).fromDatastore(dataStoreMock).withSettings(new EnumMap<>(ViewProps.class)).build());

        when(camerasDatabaseMock.cameraSetExistsInIndex(125)).thenReturn(true);
        when(camerasDatabaseMock.cameraSetExistsInSettings(125)).thenReturn(true);
        when(camerasDatabaseMock.getViewsForCameraSet(125)).thenReturn(currentViews);


        // when
        camPatcher.apply(camPatchDto);

        // then
        assertThat(currentViews.get(0).getSettings().get(VIEW_POSITION_X)).isEqualTo(1500L);
    }

    @Test
    void apply_whenCameraSetDoesNotExist_andReferenceSetDoesNotExist_shouldThrowException() {
        // given
        CamPatcher camPatcher = new CamPatcher(camerasDatabaseMock);
        ViewChangeDto viewChangeDto = ViewChangeDto.builder()
                .forViewKind(Cockpit)
                .addProp(VIEW_POSITION_X, "1500")
                .build();
        SetChangeDto setChangeObject = SetChangeDto.builder()
                .withSetIdentifier(1250)
                .addChanges(singletonList(viewChangeDto))
                .build();
        CamPatchDto camPatchDto = CamPatchDto.builder().addChanges(singletonList(setChangeObject)).build();

        when(camerasDatabaseMock.getViewEntriesAsStream()).thenReturn(Stream.empty());

        // when-then
        assertThrows(IllegalStateException.class,
                () -> camPatcher.apply(camPatchDto));
    }

    // TODO enable
    @Test
    @Disabled
    void apply_whenCameraSetDoesNotExist_shouldCloneReferenceSet() {
        // given
        CamPatcher camPatcher = new CamPatcher(camerasDatabaseMock);
        ViewChangeDto viewChangeDto = ViewChangeDto.builder()
                .forViewKind(Cockpit)
                .addProp(VIEW_POSITION_X, "1500")
                .build();
        SetChangeDto setChangeObject = SetChangeDto.builder()
                .withSetIdentifier(1250)
                .addChanges(singletonList(viewChangeDto))
                .build();
        CamPatchDto camPatchDto = CamPatchDto.builder().addChanges(singletonList(setChangeObject)).build();

        DataStore refDataStoreMock = mock(DataStore.class);
        DataStore clonedDataStoreMock = mock(DataStore.class);
        Map<Integer, List<CameraView>> viewsMapBeforeCloning = new HashMap<>(1);
        viewsMapBeforeCloning.put(10000, singletonList(CameraView.builder().build()));
        Map<Integer, List<CameraView>> storeMapAfterCloning = new HashMap<>(2);
        storeMapAfterCloning.put(10000, singletonList(CameraView.builder().build()));
        storeMapAfterCloning.put(1250, singletonList(CameraView.builder().build()));
        Map<Integer, Short> indexMapBeforeCloning = new HashMap<>(1);
        indexMapBeforeCloning.put(10000, (short) 12);
        Map<Integer, Short> indexMapAfterCloning = new HashMap<>(2);
        indexMapAfterCloning.put(10000, (short) 12);
        indexMapAfterCloning.put(1250, (short) 4);

        //noinspection unchecked
        when(camerasDatabaseMock.getIndexEntriesAsStream()).thenReturn(
                indexMapBeforeCloning.entrySet().stream(),
                indexMapAfterCloning.entrySet().stream());
        //noinspection unchecked
        when(camerasDatabaseMock.getViewEntriesAsStream()).thenReturn(
                viewsMapBeforeCloning.entrySet().stream(),
                viewsMapBeforeCloning.entrySet().stream(),
                storeMapAfterCloning.entrySet().stream(),
                storeMapAfterCloning.entrySet().stream());
        when(refDataStoreMock.copy()).thenReturn(clonedDataStoreMock);
        when(clonedDataStoreMock.getInteger("type")).thenReturn(of(23L));


        // when
        camPatcher.apply(camPatchDto);

        // then
        verify(camerasDatabaseMock, times(4)).getViewEntriesAsStream();
        verify(clonedDataStoreMock).addInteger32("viewPositionX", 1500L);
    }
}
