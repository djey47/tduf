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

        when(camerasParserMock.getCameraViews()).thenReturn(storeMap);
        when(camerasParserMock.getViewProps(dataStoreMock)).thenReturn(new EnumMap<>(ViewProps.class));
        when(camerasParserMock.getDataStore()).thenReturn(dataStoreMock);
        when(dataStoreMock.getInteger("type")).thenReturn(of(23L));


        // when
        camPatcher.apply(camPatchDto);

        // then
        verify(dataStoreMock).addInteger("viewPositionX", 1500L);
        verify(camerasParserMock).getDataStore();
    }

    @Test
    // TODO implement
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

        DataStore dataStoreMock = mock(DataStore.class);
        Map<Long, List<DataStore>> storeMap = new HashMap<>(1);
        storeMap.put(125L, singletonList(dataStoreMock));

        when(camerasParserMock.getCameraViews()).thenReturn(storeMap);
        when(camerasParserMock.getViewProps(dataStoreMock)).thenReturn(new EnumMap<>(ViewProps.class));
        when(camerasParserMock.getDataStore()).thenReturn(dataStoreMock);
        when(dataStoreMock.getInteger("type")).thenReturn(of(23L));


        // when
        camPatcher.apply(camPatchDto);

        // then
        verify(dataStoreMock).addInteger("viewPositionX", 1500L);
        verify(camerasParserMock).getDataStore();
    }
}
