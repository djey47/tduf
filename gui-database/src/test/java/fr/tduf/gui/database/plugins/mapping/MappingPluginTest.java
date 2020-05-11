package fr.tduf.gui.database.plugins.mapping;

import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.gui.database.plugins.mapping.domain.MappingEntry;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class MappingPluginTest {
    @Mock
    private BulkDatabaseMiner minerMock;

    @Mock
    private MainStageController controllerMock;

    @Mock
    private Property<BankMap> bankMapProperty;

    @Mock
    private StringProperty errorMessageProperty;

    @Mock
    private BooleanProperty errorProperty;

    @Mock
    private OnTheFlyContext onTheFlyContextMock;

    @InjectMocks
    private MappingPlugin mappingPlugin;

    private final EditorContext editorContext = new EditorContext();
    private final ObservableList<MappingEntry> files = FXCollections.observableArrayList();

    @BeforeEach
    void setUp() {
        initMocks(this);

        editorContext.setMainStageController(controllerMock);
        editorContext.setMiner(minerMock);

        mappingPlugin.getMappingContext().setErrorProperty(errorProperty);
        mappingPlugin.getMappingContext().setErrorMessageProperty(errorMessageProperty);
        mappingPlugin.setEditorContext(editorContext);

        when(bankMapProperty.getValue()).thenReturn(new BankMap());

        files.clear();

        when(onTheFlyContextMock.getFiles()).thenReturn(files);
        when(onTheFlyContextMock.getContentEntryIndexProperty()).thenReturn(new SimpleObjectProperty<>(0));
    }
    
    @Test
    void onInit_whenNoMappingFile_shouldThrowException_andNotAttemptLoading() {
        // given
        editorContext.setGameLocation(".");

        // when-then
        assertThrows(IOException.class, () -> mappingPlugin.onInit("MAPPING", editorContext));
        assertThat(mappingPlugin.getMappingContext().isPluginLoaded()).isFalse();
    }

    @Test
    void onSave_whenNoMappingLoaded_shouldNotAttemptSaving() throws IOException {
        // given-when-then
        mappingPlugin.onSave();
    }

    @Test
    void renderControls_whenNoMappingLoaded_shouldReturnEmptyComponent() {
        // given
        mappingPlugin.getMappingContext().setPluginLoaded(false);
        OnTheFlyContext onTheFlyContext = new OnTheFlyContext();

        // when
        Node actualNode = mappingPlugin.renderControls(onTheFlyContext);

        // then
        assertThat(actualNode).isInstanceOf(HBox.class);
        assertThat(((HBox) actualNode).getChildren()).isEmpty();
    }
    
    @Test
    void resolveMappingFilePath() {
        // given-when
        Path actualPath = mappingPlugin.resolveMappingFilePath("/tdu");
        
        // then
        assertThat(actualPath).isEqualTo(Paths.get("/", "tdu", "Euro", "Bnk", "Bnk1.map"));
    }    
    
    @Test
    void resolveBankFilePath() {
        // given-when
        Path actualPath = mappingPlugin.resolveBankFilePath("/tdu", "Vehicules/A3_V6.bnk");
        
        // then
        assertThat(actualPath).isEqualTo(Paths.get("/", "tdu", "Euro", "Bnk", "Vehicules", "A3_V6.bnk"));
    }    
    
    @Test
    void createMappingEntry_forExteriorModelBank() {
        // given

        // when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("A3_V6", EXT_3D, onTheFlyContextMock);
        
        // then
        assertThat(actualEntry.getKind()).isEqualTo(EXT_3D.getDescription());
        assertThat(actualEntry.getPath()).isEqualTo(Paths.get("Vehicules", "A3_V6.bnk").toString());
        assertThat(actualEntry.isExists()).isFalse();
        assertThat(actualEntry.isRegistered()).isFalse();
    }    
    
    @Test
    void createMappingEntry_forShopModelBank() {
        // given-when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("ECD_2B2_7555", SHOP_EXT_3D, onTheFlyContextMock);
        
        // then
        assertThat(actualEntry.getKind()).isEqualTo(SHOP_EXT_3D.getDescription());
        assertThat(actualEntry.getPath()).isEqualTo(Paths.get("Level", "Hawai", "Spots", "ecd_2b2.bnk").toString());
        assertThat(actualEntry.isExists()).isFalse();
        assertThat(actualEntry.isRegistered()).isFalse();
    }    
    
    @Test
    void createMappingEntry_forClothesModelBank() {
        // given
        ContentEntryDto clothesEntryMock = mock(ContentEntryDto.class);
        ContentEntryDto brandsEntryMock = mock(ContentEntryDto.class);
        ContentItemDto clothesItemMock = mock(ContentItemDto.class);
        ContentItemDto brandsItemMock = mock(ContentItemDto.class);
        ResourceEntryDto brandsResourceMock = mock(ResourceEntryDto.class);

        when(clothesEntryMock.getItemAtRank(5)).thenReturn(of(clothesItemMock));
        when(brandsEntryMock.getItemAtRank(2)).thenReturn(of(brandsItemMock));
        when(clothesItemMock.getRawValue()).thenReturn("B");
        when(brandsItemMock.getRawValue()).thenReturn("R");
        when(brandsResourceMock.pickValue()).thenReturn(of("ELLSON"));

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, null)).thenReturn(of(clothesEntryMock));
        when(minerMock.getContentEntryFromTopicWithReference("B", BRANDS)).thenReturn(of(brandsEntryMock));
        when(minerMock.getResourceEntryFromTopicAndReference(BRANDS, "R")).thenReturn(of(brandsResourceMock));


        // when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("M_SHIRT_ELLSON", CLOTHES_3D, onTheFlyContextMock);


        // then
        assertThat(actualEntry.getKind()).isEqualTo(CLOTHES_3D.getDescription());
        assertThat(actualEntry.getPath()).isEqualTo(Paths.get("Avatar", "CLOTHES", "ELLSON", "M_SHIRT_ELLSON.bnk").toString());
        assertThat(actualEntry.isExists()).isFalse();
        assertThat(actualEntry.isRegistered()).isFalse();
    }

    @Test
    void createMappingEntry_forRimsModelBank() {
        // given
        ContentEntryDto rimsEntryMock = mock(ContentEntryDto.class);
        ContentItemDto rimsItemMock = mock(ContentItemDto.class);
        ResourceEntryDto rimsResourceMock = mock(ResourceEntryDto.class);

        when(rimsEntryMock.getItemAtRank(13)).thenReturn(of(rimsItemMock));
        when(rimsItemMock.getRawValue()).thenReturn("B");
        when(rimsResourceMock.pickValue()).thenReturn(of("AC"));

        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, null)).thenReturn(of(rimsEntryMock));
        when(minerMock.getResourceEntryFromTopicAndReference(null, "B")).thenReturn(of(rimsResourceMock));


        // when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("AC_427_F01", FRONT_RIMS_3D, onTheFlyContextMock);


        // then
        assertThat(actualEntry.getKind()).isEqualTo(FRONT_RIMS_3D.getDescription());
        assertThat(actualEntry.getPath()).isEqualTo(Paths.get("Vehicules", "Rim", "AC", "AC_427_F01.bnk").toString());
        assertThat(actualEntry.isExists()).isFalse();
        assertThat(actualEntry.isRegistered()).isFalse();
    }

    @Test
    void refreshMapping_whenHandledTopicAndFieldRank_shouldClearEntryList_andAddEntries_withErrors() {
        // given
        MappingEntry existingEntry = new MappingEntry("", "", true, true);
        files.add(existingEntry);

        when(onTheFlyContextMock.getCurrentTopic()).thenReturn(CAR_PHYSICS_DATA);
        when(onTheFlyContextMock.getRemoteTopic()).thenReturn(CAR_PHYSICS_DATA);
        when(onTheFlyContextMock.getFieldRank()).thenReturn(9); //FIELD_RANK_CAR_FILE_NAME

        String resourceRef = "RES";
        ResourceEntryDto resourceEntry = ResourceEntryDto.builder().forReference(resourceRef).withDefaultItem("VALUE").build();
        when(minerMock.getResourceEntryFromTopicAndReference(CAR_PHYSICS_DATA, resourceRef)).thenReturn(of(resourceEntry));


        // when
        mappingPlugin.refreshMapping(resourceRef, onTheFlyContextMock);


        // then
        assertThat(files).hasSize(3);
        final Optional<MappingEntry> firstEntry = files.stream().findFirst();
        assertThat(firstEntry).isPresent();
        firstEntry.ifPresent((entry) -> {
            assertThat(entry.getKind()).isEqualTo("Exterior 3D model");
            assertThat(entry.getPath()).isEqualTo("Vehicules/VALUE.bnk");
        });

        verify(errorProperty).setValue(true);
        verify(errorMessageProperty).setValue("One of listed files is not registered into Bnk1.map, may not be taken into account by the game.");
    }

    @Test
    void refreshMapping_whenHandledTopicAndUnhandledFieldRank_shouldClearEntryList_andInitErrorProps() {
        // given
        MappingEntry existingEntry = new MappingEntry("", "", true, true);
        files.add(existingEntry);

        when(onTheFlyContextMock.getCurrentTopic()).thenReturn(CAR_PHYSICS_DATA);
        when(onTheFlyContextMock.getRemoteTopic()).thenReturn(CAR_PHYSICS_DATA);
        when(onTheFlyContextMock.getFieldRank()).thenReturn(0);

        String resourceRef = "RES";


        // when
        mappingPlugin.refreshMapping(resourceRef, onTheFlyContextMock);


        // then
        assertThat(files).isEmpty();

        verify(errorProperty).setValue(false);
        verify(errorMessageProperty).setValue("");
    }

    @Test
    void refreshMapping_whenUnhandledTopic_shouldClearEntryList_andResetErrorProps() {
        // given
        MappingEntry existingEntry = new MappingEntry("", "", true, true);
        files.add(existingEntry);

        when(onTheFlyContextMock.getCurrentTopic()).thenReturn(BRANDS);


        // when
        mappingPlugin.refreshMapping("RES", onTheFlyContextMock);


        // then
        assertThat(files).isEmpty();

        verifyNoInteractions(minerMock);
        verify(errorProperty).setValue(isNull());
        verify(errorMessageProperty).setValue(isNull());
    }
}
