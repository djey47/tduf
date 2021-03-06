package fr.tduf.libunlimited.low.files.gfx.materials.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MaterialsHelperTest {

    @Mock
    private BulkDatabaseMiner minerMock;

    @BeforeEach
    void setUp() {
        initMocks(this);
        initResourceMocks();
    }

    @Test
    void buildNormalizedDictionary_shouldQueryResources() {
        // given-when
        Map<String, String> actualDic = MaterialsHelper.buildNormalizedDictionary(minerMock);

        // then
        assertThat(actualDic).hasSize(2);
        assertThat(actualDic).containsEntry("\u0087ALUE___", "Value___1");
        assertThat(actualDic).containsEntry("\u0088ALUE___", "Value___2");
    }

    @Test
    void updateNormalizedDictionary_shouldQueryResources() {
        // given
        Map<String, String> dicToUpdate = new HashMap<>();

        // when
        MaterialsHelper.updateNormalizedDictionary(dicToUpdate, minerMock);

        // then
        assertThat(dicToUpdate).hasSize(2);
        assertThat(dicToUpdate).containsEntry("\u0087ALUE___", "Value___1");
        assertThat(dicToUpdate).containsEntry("\u0088ALUE___", "Value___2");
    }

    @Test
    void isExistingMaterialNameInResources_whenNameIsNull_shouldReturnFalse() {
        // given-when-then
        assertThat(MaterialsHelper.isExistingMaterialNameInResources(null, INTERIOR, minerMock)).isFalse();
    }

    @Test
    void isExistingMaterialNameInResources_whenNonExistingResource_shouldReturnFalse() {
        // given-when-then
        assertThat(MaterialsHelper.isExistingMaterialNameInResources("Value___1", INTERIOR, minerMock)).isFalse();
    }

    @Test
    void isExistingMaterialNameInResources_whenExistingResource_shouldReturnTrue() {
        // given-when-then
        assertThat(MaterialsHelper.isExistingMaterialNameInResources("Value___1", CAR_COLORS, minerMock)).isTrue();
    }

    @Test
    void getResourceRefForMaterialName_whenNonExistingResource_shouldReturnAbsent() {
        // given-when-then
        assertThat(MaterialsHelper.getResourceRefForMaterialName("Value___1", INTERIOR, minerMock)).isEmpty();
    }

    @Test
    void getResourceRefForMaterialName_shouldReturnExistingRef() {
        // given-when
        Optional<String> actualRef = MaterialsHelper.getResourceRefForMaterialName("Value___1", CAR_COLORS, minerMock);

        // then
        assertThat(actualRef).contains("REF1");
    }

    @Test
    void getResourceRefForMaterialName_shouldReturnExistingRef_caseUnsensitive() {
        // given-when
        Optional<String> actualRef = MaterialsHelper.getResourceRefForMaterialName("VALUE___1", CAR_COLORS, minerMock);

        // then
        assertThat(actualRef).contains("REF1");
    }

    @Test
    void resolveNoMaterialReference_whenSupportedTopicsshouldReturnRightRefs() {
        // given-when-then
        assertThat(MaterialsHelper.resolveNoMaterialReference(CAR_COLORS)).isEqualTo("53356127");
        assertThat(MaterialsHelper.resolveNoMaterialReference(INTERIOR)).isEqualTo("53364643");
    }

    @Test
    void resolveNoMaterialReference_whenUnsupprtedTopic_shouldThrowException() {
        // given-when-then
        //noinspection ResultOfMethodCallIgnored
        assertThrows(IllegalArgumentException.class,
                () -> MaterialsHelper.resolveNoMaterialReference(CAR_PHYSICS_DATA));
    }

    @Test
    void getColorSettingsKeyStream_shouldReturnStreamOfAllColorKeys() {
        // given-when
        Stream<String> actualStream = MaterialsHelper.getColorSettingsKeyStream("groupKeyName.", "settingKeyName");

        // then
        assertThat(actualStream).containsExactly(
                "groupKeyName.settingKeyNameR",
                "groupKeyName.settingKeyNameG",
                "groupKeyName.settingKeyNameB",
                "groupKeyName.settingKeyNameO");
    }

    @Test
    @Disabled("Would require mocking static method from MaterialsWriter")
    void saveMaterialDefinitions() {}

    private void initResourceMocks() {
        DbResourceDto carColorsResources = DbResourceDto.builder()
                .atVersion("1.0")
                .containingEntries(singletonList(ResourceEntryDto.builder().forReference("REF1").withDefaultItem("Value___1").build()))
                .build();
        DbResourceDto interiorResources = DbResourceDto.builder()
                .atVersion("1.0")
                .containingEntries(asList(
                        ResourceEntryDto.builder().forReference("REF2").withDefaultItem("Value___2").build(),
                        ResourceEntryDto.builder().forReference("REF3").withDefaultItem("VALUE___2").build()))
                .build();
        when(minerMock.getResourcesFromTopic(CAR_COLORS)).thenReturn(of(carColorsResources));
        when(minerMock.getResourcesFromTopic(INTERIOR)).thenReturn(of(interiorResources));
    }
}