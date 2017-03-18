package fr.tduf.gui.database.plugins.mapping;

import fr.tduf.gui.database.plugins.common.EditorContext;
import fr.tduf.gui.database.plugins.mapping.domain.MappingEntry;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.CLOTHES_3D;
import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.EXT_3D;
import static fr.tduf.libunlimited.low.files.banks.domain.MappedFileKind.SHOP_EXT_3D;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MappingPluginTest {
    @Mock
    private Property<BankMap> bankMapProperty;
    
    @InjectMocks
    private MappingPlugin mappingPlugin;
    
    private final EditorContext context = new EditorContext();

    @BeforeEach
    void setUp() {
        initMocks(this);
    }
    
    @Test
    void onInit_whenNoMapping_shouldNotAttemptLoading() throws IOException {
        // given
        context.setGameLocation(".");

        // when
        mappingPlugin.onInit(context);

        // then
        assertThat(context.getMappingContext().isPluginLoaded()).isFalse();
    }

    @Test
    void onSave_whenNoCamerasLoaded_shouldNotAttemptSaving() throws IOException {
        // given-when-then
        mappingPlugin.onSave(context);
    }

    @Test
    void renderControls_whenNoCamerasLoaded_shouldReturnEmptyComponent() throws IOException {
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
        when(bankMapProperty.getValue()).thenReturn(new BankMap());
        
        // when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("A3_V6", EXT_3D, "/tdu", context);
        
        // then
        assertThat(actualEntry.getKind()).isEqualTo(EXT_3D.getDescription());
        assertThat(actualEntry.getPath()).isEqualTo(Paths.get("Vehicules", "A3_V6.bnk").toString());
        assertThat(actualEntry.isExists()).isFalse();
        assertThat(actualEntry.isRegistered()).isFalse();
    }    
    
    @Test
    void createMappingEntry_forShopModelBank() {
        // given
        when(bankMapProperty.getValue()).thenReturn(new BankMap());
        
        // when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("ECD_2B2_7555", SHOP_EXT_3D, "/tdu", context);
        
        // then
        assertThat(actualEntry.getKind()).isEqualTo(SHOP_EXT_3D.getDescription());
        assertThat(actualEntry.getPath()).isEqualTo(Paths.get("Level", "Hawai", "Spots", "ecd_2b2.bnk").toString());
        assertThat(actualEntry.isExists()).isFalse();
        assertThat(actualEntry.isRegistered()).isFalse();
    }    
    
    @Test
    void createMappingEntry_forClothesModelBank() {
        // given
        when(bankMapProperty.getValue()).thenReturn(new BankMap());
        
        // when
        MappingEntry actualEntry = mappingPlugin.createMappingEntry("M_SHIRT_ELLSON", CLOTHES_3D, "/tdu", context);
        
        // then
        assertThat(actualEntry.getKind()).isEqualTo(CLOTHES_3D.getDescription());
        assertThat(actualEntry.getPath()).isEqualTo(Paths.get("Avatar", "CLOTHES", "ELLSON", "M_SHIRT_ELLSON.bnk").toString());
        assertThat(actualEntry.isExists()).isFalse();
        assertThat(actualEntry.isRegistered()).isFalse();
    }
}
