package fr.tduf.libunlimited.high.files.bin.cameras.patcher;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.ViewChangeDto;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Cockpit;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.VIEW_POSITION_X;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class CamPatcherTest {

    @Mock
    private CamerasParser camerasParserMock;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void new_whenNullParser_shouldThrowException() {
        // given-when-then
        assertThrows(NullPointerException.class,
                () -> new CamPatcher(null));
    }

    @Test
    void apply_whenNullPatchObject_shouldThrowException() {
        // given-when
        CamPatcher camPatcher = new CamPatcher(camerasParserMock);

        // then
        assertThrows(NullPointerException.class,
                () -> camPatcher.apply(null));
    }

    @Test
    void apply_whenCameraSetExists_shouldInvokeParser() {
        // given
        CamPatcher camPatcher = new CamPatcher(camerasParserMock);
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
        Map<Long, List<DataStore>> storeMap = new HashMap<>(1);
        storeMap.put(125L, singletonList(dataStoreMock));
        Map<Long, Short> indexMap = new HashMap<>(1);
        indexMap.put(125L, (short) 4);

        when(camerasParserMock.getCameraIndex()).thenReturn(indexMap);
        when(camerasParserMock.getCameraViews()).thenReturn(storeMap);
        when(camerasParserMock.getViewProps(dataStoreMock)).thenReturn(new EnumMap<>(ViewProps.class));
        when(camerasParserMock.getDataStore()).thenReturn(dataStoreMock);
        when(dataStoreMock.getInteger("type")).thenReturn(of(23L));


        // when
        camPatcher.apply(camPatchDto);

        // then
        verify(dataStoreMock).addInteger("viewPositionX", 1500L);
    }

    @Test
    void apply_whenCameraSetDoesNotExist_andReferenceSetDoesNotExist_shouldThrowException() {
        // given
        CamPatcher camPatcher = new CamPatcher(camerasParserMock);
        ViewChangeDto viewChangeDto = ViewChangeDto.builder()
                .forViewKind(Cockpit)
                .addProp(VIEW_POSITION_X, "1500")
                .build();
        SetChangeDto setChangeObject = SetChangeDto.builder()
                .withSetIdentifier(1250)
                .addChanges(singletonList(viewChangeDto))
                .build();
        CamPatchDto camPatchDto = CamPatchDto.builder().addChanges(singletonList(setChangeObject)).build();

        when(camerasParserMock.getCameraViews()).thenReturn(new HashMap<>(0));

        // when-then
        assertThrows(IllegalStateException.class,
                () -> camPatcher.apply(camPatchDto));
    }

    @Test
    void apply_whenCameraSetDoesNotExist_shouldCloneReferenceSet() {
        // given
        CamPatcher camPatcher = new CamPatcher(camerasParserMock);
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
        Map<Long, List<DataStore>> storeMapBeforeCloning = new HashMap<>(1);
        storeMapBeforeCloning.put(10000L, singletonList(refDataStoreMock));
        Map<Long, List<DataStore>> storeMapAfterCloning = new HashMap<>(2);
        storeMapAfterCloning.put(10000L, singletonList(refDataStoreMock));
        storeMapAfterCloning.put(1250L, singletonList(clonedDataStoreMock));
        Map<Long, Short> indexMapBeforeCloning = new HashMap<>(1);
        indexMapBeforeCloning.put(10000L, (short) 12);
        Map<Long, Short> indexMapAfterCloning = new HashMap<>(2);
        indexMapAfterCloning.put(10000L, (short) 12);
        indexMapAfterCloning.put(1250L, (short) 4);

        //noinspection unchecked
        when(camerasParserMock.getCameraIndex()).thenReturn(indexMapBeforeCloning, indexMapAfterCloning);
        //noinspection unchecked
        when(camerasParserMock.getCameraViews()).thenReturn(storeMapBeforeCloning, storeMapBeforeCloning, storeMapBeforeCloning, storeMapBeforeCloning, storeMapAfterCloning);
        when(camerasParserMock.getViewProps(clonedDataStoreMock)).thenReturn(new EnumMap<>(ViewProps.class));
        when(camerasParserMock.getDataStore()).thenReturn(refDataStoreMock);
        when(refDataStoreMock.copy()).thenReturn(clonedDataStoreMock);
        when(clonedDataStoreMock.getInteger("type")).thenReturn(of(23L));


        // when
        camPatcher.apply(camPatchDto);

        // then
        verify(clonedDataStoreMock).addInteger("viewPositionX", 1500L);
    }
}
