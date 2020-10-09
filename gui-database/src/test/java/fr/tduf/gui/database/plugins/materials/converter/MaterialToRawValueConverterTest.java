package fr.tduf.gui.database.plugins.materials.converter;

import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.Material;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import javafx.beans.property.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_COLORS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.INTERIOR;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


class MaterialToRawValueConverterTest {
    private static final Function<String, String> FULL_NAME_PROVIDER = (normalizedName) -> normalizedName + "_FULL";

    @Mock
    private Property<MaterialDefs> materialDefsPropertyMock;

    @Mock
    private EditorContext editorContextMock;

    @Mock
    private BulkDatabaseMiner databaseMinerMock;

    @BeforeEach
    void setUp() {
        initMocks(this);

        when(editorContextMock.getMiner()).thenReturn(databaseMinerMock);

        when(databaseMinerMock.getResourcesFromTopic(any())).thenReturn(of(getResources()));
        when(databaseMinerMock.getLocalizedResourceValueFromTopicAndReference(anyString(), any(), any())).thenReturn(of("NORMALIZ"));

        when(materialDefsPropertyMock.getValue()).thenReturn(getMaterialDefs());
    }

    @Test
    void toString_whenMaterialIsNull_shouldReturnEmptyString() {
        // given
        MaterialToRawValueConverter converterForCarColors = prepareConverterForTopic(CAR_COLORS);

        // when-then
        assertThat(converterForCarColors.toString(null)).isEmpty();
    }

    @Test
    void toString_whenResourceEntryNotFound_shouldReturnNoMaterialResourceRef() {
        // given
        MaterialToRawValueConverter converterForCarColors = prepareConverterForTopic(CAR_COLORS);
        MaterialToRawValueConverter interiorsForCarColors = prepareConverterForTopic(INTERIOR);
        Material material = Material.builder()
                .withName("NA_MAT")
                .build();

        // when
        String carColorsMaterialAsString = converterForCarColors.toString(material);
        String interiorsMaterialAsString = interiorsForCarColors.toString(material);

        // then
        assertThat(carColorsMaterialAsString).isEqualTo("53356127");
        assertThat(interiorsMaterialAsString).isEqualTo("53364643");
    }

    @Test
    void toString_whenResourceEntryFoundForNormalizedName_shouldReturnResourceRef() {
        // given
        MaterialToRawValueConverter converterForCarColors = prepareConverterForTopic(CAR_COLORS);
        Material material = Material.builder()
                .withName("NORMALIZ")
                .build();

        // when
        String carColorsMaterialAsString = converterForCarColors.toString(material);

        // then
        assertThat(carColorsMaterialAsString).isEqualTo("RESOURCE_REF");
    }

    @Test
    void toString_whenResourceEntryFoundForFullName_shouldReturnResourceRef() {
        // given
        MaterialToRawValueConverter converterForCarColors = prepareConverterForTopic(CAR_COLORS);
        Material material = Material.builder()
                .withName("MATERIAL")
                .build();

        // when
        String carColorsMaterialAsString = converterForCarColors.toString(material);

        // then
        assertThat(carColorsMaterialAsString).isEqualTo("RESOURCE_REF2");
    }

    @Test
    void fromString_whenNoMaterialResourceRef_shouldReturnNull() {
        // given
        when(databaseMinerMock.getLocalizedResourceValueFromTopicAndReference(anyString(), any(), any())).thenReturn(of("??"));
        MaterialToRawValueConverter converter = prepareConverterForTopic(CAR_COLORS);

        // when-then
        assertThat(converter.fromString("53356127")).isNull();
    }

    @Test
    void fromString_whenMaterialDoesNotExistWithResourceValue_shouldReturnNull() {
        // given
        when(databaseMinerMock.getLocalizedResourceValueFromTopicAndReference(anyString(), any(), any())).thenReturn(of("NORMALI2"));
        MaterialToRawValueConverter converter = prepareConverterForTopic(CAR_COLORS);

        // when-then
        assertThat(converter.fromString("RESOURCE_REF")).isNull();
    }

    @Test
    void fromString_whenMaterialExistsWithResourceValue_shouldReturnIt() {
        // given
        MaterialToRawValueConverter converter = prepareConverterForTopic(CAR_COLORS);

        // when
        Material actualMaterial = converter.fromString("RESOURCE_REF");

        // then
        assertThat(actualMaterial).extracting(Material::getName).isEqualTo("NORMALIZ");
    }

    private MaterialToRawValueConverter prepareConverterForTopic(DbDto.Topic topic) {
        return new MaterialToRawValueConverter(materialDefsPropertyMock, editorContextMock, topic, FULL_NAME_PROVIDER);
    }

    private static MaterialDefs getMaterialDefs() {
        List<Material> materials = singletonList(
                Material.builder()
                        .withName("NORMALIZ")
                        .build());
        return MaterialDefs.builder().withMaterials(materials).build();
    }

    private static DbResourceDto getResources() {
        Collection<ResourceEntryDto> entries = asList(
                ResourceEntryDto.builder()
                        .forReference("RESOURCE_REF")
                        .withDefaultItem("NORMALIZ")
                        .build(),
                ResourceEntryDto.builder()
                        .forReference("RESOURCE_REF2")
                        .withDefaultItem("MATERIAL_FULL")
                        .build());
        return DbResourceDto.builder()
                .atVersion("1")
                .containingEntries(entries)
                .build();
    }
}