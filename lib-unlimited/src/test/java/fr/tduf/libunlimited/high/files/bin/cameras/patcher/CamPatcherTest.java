package fr.tduf.libunlimited.high.files.bin.cameras.patcher;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.ViewChangeDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfoEnhanced;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraViewEnhanced;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.VIEW_POSITION_X;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

// TODO enable and fix tests
class CamPatcherTest {

    @Mock
    private CameraInfoEnhanced cameraInfoEnhancedMock;

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
        CamPatcher camPatcher = new CamPatcher(cameraInfoEnhancedMock);

        // then
        assertThrows(NullPointerException.class,
                () -> camPatcher.apply(null));
    }

    @Test
    @Disabled
    void apply_whenCameraSetExists_shouldInvokeParser() {
        // given
        CamPatcher camPatcher = new CamPatcher(cameraInfoEnhancedMock);
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
        Map<Integer, List<CameraViewEnhanced>> storeMap = new HashMap<>(1);
        storeMap.put(125, singletonList(CameraViewEnhanced.builder().build()));
        Map<Integer, Short> indexMap = new HashMap<>(1);
        indexMap.put(125, (short) 4);

        when(cameraInfoEnhancedMock.getIndexEntriesAsStream()).thenReturn(indexMap.entrySet().stream());
        when(cameraInfoEnhancedMock.getViewEntriesAsStream()).thenReturn(storeMap.entrySet().stream());
//        when(cameraInfoEnhancedMock.getgetViewProps(dataStoreMock)).thenReturn(new EnumMap<>(ViewProps.class));
//        when(cameraInfoEnhancedMock.getDataStore()).thenReturn(dataStoreMock);
        when(dataStoreMock.getInteger("type")).thenReturn(of(23L));


        // when
        camPatcher.apply(camPatchDto);

        // then
        verify(dataStoreMock).addInteger32("viewPositionX", 1500L);
    }

    @Test
    void apply_whenCameraSetDoesNotExist_andReferenceSetDoesNotExist_shouldThrowException() {
        // given
        CamPatcher camPatcher = new CamPatcher(cameraInfoEnhancedMock);
        ViewChangeDto viewChangeDto = ViewChangeDto.builder()
                .forViewKind(Cockpit)
                .addProp(VIEW_POSITION_X, "1500")
                .build();
        SetChangeDto setChangeObject = SetChangeDto.builder()
                .withSetIdentifier(1250)
                .addChanges(singletonList(viewChangeDto))
                .build();
        CamPatchDto camPatchDto = CamPatchDto.builder().addChanges(singletonList(setChangeObject)).build();

        when(cameraInfoEnhancedMock.getViewEntriesAsStream()).thenReturn(Stream.empty());

        // when-then
        assertThrows(IllegalStateException.class,
                () -> camPatcher.apply(camPatchDto));
    }

    @Test
    @Disabled
    void apply_whenCameraSetDoesNotExist_shouldCloneReferenceSet() {
        // given
        CamPatcher camPatcher = new CamPatcher(cameraInfoEnhancedMock);
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
        Map<Integer, List<CameraViewEnhanced>> viewsMapBeforeCloning = new HashMap<>(1);
        viewsMapBeforeCloning.put(10000, singletonList(CameraViewEnhanced.builder().build()));
        Map<Integer, List<CameraViewEnhanced>> storeMapAfterCloning = new HashMap<>(2);
        storeMapAfterCloning.put(10000, singletonList(CameraViewEnhanced.builder().build()));
        storeMapAfterCloning.put(1250, singletonList(CameraViewEnhanced.builder().build()));
        Map<Integer, Short> indexMapBeforeCloning = new HashMap<>(1);
        indexMapBeforeCloning.put(10000, (short) 12);
        Map<Integer, Short> indexMapAfterCloning = new HashMap<>(2);
        indexMapAfterCloning.put(10000, (short) 12);
        indexMapAfterCloning.put(1250, (short) 4);

        //noinspection unchecked
        when(cameraInfoEnhancedMock.getIndexEntriesAsStream()).thenReturn(
                indexMapBeforeCloning.entrySet().stream(),
                indexMapAfterCloning.entrySet().stream());
        //noinspection unchecked
        when(cameraInfoEnhancedMock.getViewEntriesAsStream()).thenReturn(
                viewsMapBeforeCloning.entrySet().stream(),
                viewsMapBeforeCloning.entrySet().stream(),
                storeMapAfterCloning.entrySet().stream(),
                storeMapAfterCloning.entrySet().stream());
//        when(cameraInfoEnhancedMock.getViewProps(clonedDataStoreMock)).thenReturn(new EnumMap<>(ViewProps.class));
//        when(cameraInfoEnhancedMock.getDataStore()).thenReturn(refDataStoreMock);
        when(refDataStoreMock.copy()).thenReturn(clonedDataStoreMock);
        when(clonedDataStoreMock.getInteger("type")).thenReturn(of(23L));


        // when
        camPatcher.apply(camPatchDto);

        // then
        verify(cameraInfoEnhancedMock, times(4)).getViewEntriesAsStream();
        verify(clonedDataStoreMock).addInteger32("viewPositionX", 1500L);
    }
}
