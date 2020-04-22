package fr.tduf.gui.database.plugins.mapping;

import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.gui.database.plugins.mapping.domain.MappingEntry;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
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

    @InjectMocks
    private MappingPlugin mappingPlugin;

    private final EditorContext context = new EditorContext();

    @BeforeEach
    void setUp() {
        initMocks(this);

        context.setMainStageController(controllerMock);
        context.setMiner(minerMock);
        context.getMappingContext().setErrorProperty(errorProperty);
        context.getMappingContext().setErrorMessageProperty(errorMessageProperty);

        mappingPlugin.setEditorContext(context);

        when(bankMapProperty.getValue()).thenReturn(new BankMap());
    }
    
    @Test
    void onInit_whenNoMappingFile_shouldThrowException_andNotAttemptLoading() {
        // given
        context.setGameLocation(".");

        // when-then
        assertThrows(IOException.class, () -> mappingPlugin.onInit(context));
        assertThat(context.getMappingContext().isPluginLoaded()).isFalse();
    }

    @Test
    void onSave_whenNoMappingLoaded_shouldNotAttemptSaving() throws IOException {
        // given-when-then
        mappingPlugin.onSave(context);
    }

    @Test
    void renderControls_whenNoMappingLoaded_shouldReturnEmptyComponent() {
        // given
        context.getMappingContext().setPluginLoaded(false);

        // when
        Node actualNode = mappingPlugin.renderControls(context);

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
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("A3_V6", EXT_3D, "/tdu");
        
        // then
        assertThat(actualEntry.getKind()).isEqualTo(EXT_3D.getDescription());
        assertThat(actualEntry.getPath()).isEqualTo(Paths.get("Vehicules", "A3_V6.bnk").toString());
        assertThat(actualEntry.isExists()).isFalse();
        assertThat(actualEntry.isRegistered()).isFalse();
    }    
    
    @Test
    void createMappingEntry_forShopModelBank() {
        // given-when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("ECD_2B2_7555", SHOP_EXT_3D, "/tdu");
        
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

        when(controllerMock.getCurrentEntryIndex()).thenReturn(0);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, null)).thenReturn(of(clothesEntryMock));
        when(minerMock.getContentEntryFromTopicWithReference("B", BRANDS)).thenReturn(of(brandsEntryMock));
        when(minerMock.getResourceEntryFromTopicAndReference(BRANDS, "R")).thenReturn(of(brandsResourceMock));


        // when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("M_SHIRT_ELLSON", CLOTHES_3D, "/tdu");


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

        when(controllerMock.getCurrentEntryIndex()).thenReturn(0);
        when(minerMock.getContentEntryFromTopicWithInternalIdentifier(0, null)).thenReturn(of(rimsEntryMock));
        when(minerMock.getResourceEntryFromTopicAndReference(null, "B")).thenReturn(of(rimsResourceMock));


        // when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("AC_427_F01", FRONT_RIMS_3D, "/tdu");


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
        ObservableList<MappingEntry> files = FXCollections.observableArrayList(existingEntry);

        context.setCurrentTopic(CAR_PHYSICS_DATA);
        context.setRemoteTopic(CAR_PHYSICS_DATA);
        context.setFieldRank(9); //FIELD_RANK_CAR_FILE_NAME

        String resourceRef = "RES";
        ResourceEntryDto resourceEntry = ResourceEntryDto.builder().forReference(resourceRef).withDefaultItem("VALUE").build();
        when(minerMock.getResourceEntryFromTopicAndReference(CAR_PHYSICS_DATA, resourceRef)).thenReturn(of(resourceEntry));


        // when
        mappingPlugin.refreshMapping(files, resourceRef);


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
}
