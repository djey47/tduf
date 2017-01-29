package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto.fromCouple;
import static fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher_commonTest.createPatcher;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.UID;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class DatabasePatcher_focusOnPlaceholdersTest {

    private DbDto databaseObject;
    private DatabasePatcher databasePatcher;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        databaseObject = DbDto.builder()
                .withData(DbDataDto.builder().forTopic(CAR_PHYSICS_DATA).build())
                .withStructure(DbStructureDto.builder()
                        .forTopic(CAR_PHYSICS_DATA)
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(UID)
                                .build())
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(2)
                                .fromType(RESOURCE_CURRENT_LOCALIZED)
                                .build())
                        .build())
                .withResource(DbResourceDto.builder()
                        .atVersion("1,0")
                        .withCategoryCount(1).build())
                .build();

        databasePatcher = createPatcher(singletonList(databaseObject));
    }

    @Test
     void apply_whenUpdateContents_forRef_withProperty_inValues_shouldUsePropertyValue() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName = "MYREF";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_PHYSICS_DATA)
                .asReferencePlaceholder(placeholderName)
                .withEntryValues(asList("{MYREF}", "103"))
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);
        DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register(placeholderName, "000000");

        // WHEN
        databasePatcher.applyWithProperties(patchObject, patchProperties);

        // THEN
        assertThat(databaseObject.getData().getEntries()).hasSize(1);
        assertThat(databaseObject.getData().getEntries().get(0).getItems())
                .hasSize(2)
                .extracting("rawValue")
                .containsExactly("000000", "103");
    }

    @Test
     void apply_whenUpdateContents_forRef_withProperty_inPartialValues_shouldUsePropertyValue() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName1 = "MYREF";
        final String placeholderName2 = "MYNEWREF";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_PHYSICS_DATA)
                .asReferencePlaceholder(placeholderName1)
                .withPartialEntryValues(singletonList(fromCouple(1, "{MYNEWREF}")))
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);
        DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register(placeholderName1, "000000");
        patchProperties.register(placeholderName2, "111111");

        databaseObject.getData().addEntryWithItems(asList(
                ContentItemDto.builder()
                        .ofFieldRank(1)
                        .withRawValue("000000")
                        .build(),
                ContentItemDto.builder()
                        .ofFieldRank(2)
                        .withRawValue("103")
                        .build()));

        // WHEN
        databasePatcher.applyWithProperties(patchObject, patchProperties);

        // THEN
        assertThat(databaseObject.getData().getEntries()).hasSize(1);
        assertThat(databaseObject.getData().getEntries().get(0).getItems())
                .hasSize(2)
                .extracting("rawValue")
                .containsExactly("111111", "103");
    }

    @Test
     void apply_whenUpdateContents_forRef_withoutProperty_shouldUseGeneratedValue() {
        // GIVEN
        final String placeholderName = "MYREF";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_PHYSICS_DATA)
                .asReferencePlaceholder(placeholderName)
                .withEntryValues(asList("{MYREF}", "103"))
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);


        // WHEN
        final DatabasePatchProperties actualProperties = databasePatcher.applyWithProperties(patchObject, new DatabasePatchProperties());


        // THEN
        assertThat(actualProperties.size()).isEqualTo(1);

        final String generatedValue = actualProperties.getProperty("MYREF");
        assertThat(databaseObject.getData().getEntries()).hasSize(1);
        assertThat(databaseObject.getData().getEntries().get(0).getItems())
                .hasSize(2)
                .extracting("rawValue")
                .containsOnly(generatedValue, "103");
    }

    @Test
     void apply_whenDeleteContents_forRef_withProperty_shouldUsePropertyValue() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName = "MYREF";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(DELETE)
                .forTopic(CAR_PHYSICS_DATA)
                .asReferencePlaceholder(placeholderName)
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);
        DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register(placeholderName, "000000");

        databaseObject.getData().addEntryWithItems(asList(
                ContentItemDto.builder()
                        .ofFieldRank(1)
                        .withRawValue("000000")
                        .build(),
                ContentItemDto.builder()
                        .ofFieldRank(2)
                        .build()));


        // WHEN
        databasePatcher.applyWithProperties(patchObject, patchProperties);


        // THEN
        assertThat(databaseObject.getData().getEntries()).isEmpty();
    }

    @Test
     void apply_whenUpdateResources_forRef_withProperties_shouldUsePropertiesValues() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName1 = "MYRESREF";
        final String placeholderName2 = "MYRESVAL";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(CAR_PHYSICS_DATA)
                .forLocale(FRANCE)
                .asReferencePlaceholder(placeholderName1)
                .withValuePlaceholder(placeholderName2)
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);
        DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register(placeholderName1, "000000");
        patchProperties.register(placeholderName2, "Text");

        // WHEN
        databasePatcher.applyWithProperties(patchObject, patchProperties);

        // THEN
        Optional<ResourceEntryDto> potentialEntry = databaseObject.getResource().getEntryByReference("000000");
        assertThat(potentialEntry).isPresent();
        assertThat(potentialEntry.get().getItemCount()).isEqualTo(1);
        assertThat(potentialEntry.get().getItemForLocale(FRANCE).get().getValue()).isEqualTo("Text");
    }

    @Test
     void apply_whenUpdateResources_forRef_withoutProperty_shouldUseGeneratedValue() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName1 = "MYRESREF";
        final String placeholderName2 = "MYRESVAL";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE_RES)
                .forTopic(CAR_PHYSICS_DATA)
                .forLocale(FRANCE)
                .asReferencePlaceholder(placeholderName1)
                .withValuePlaceholder(placeholderName2)
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);
        DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register(placeholderName2, "Text");


        // WHEN
        final DatabasePatchProperties actualProperties = databasePatcher.applyWithProperties(patchObject, patchProperties);


        // THEN
        assertThat(actualProperties.size()).isEqualTo(2);
        assertThat(databaseObject.getResource().getEntries()).hasSize(1);

        final String generatedValue = actualProperties.getProperty("MYRESREF");
        final ResourceEntryDto resourceEntry = databaseObject.getResource().getEntryByReference(generatedValue).get();
        assertThat(resourceEntry.getValueForLocale(FRANCE)).contains("Text");
    }

    @Test
     void apply_whenDeleteResources_forRef_withProperty_shouldUsePropertyValue() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName = "MYREF";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(DELETE_RES)
                .forTopic(CAR_PHYSICS_DATA)
                .asReferencePlaceholder(placeholderName)
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);
        DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register(placeholderName, "000000");

        databaseObject.getResource()
                .addEntryByReference("000000")
                .setValueForLocale("Text", FRANCE);


        // WHEN
        databasePatcher.applyWithProperties(patchObject, patchProperties);


        // THEN
        assertThat(databaseObject.getResource().getEntryByReference("000000")).isEmpty();
    }

    private static DbPatchDto createPatchObjectWithSingleChange(DbPatchDto.DbChangeDto changeObject) {
        return DbPatchDto.builder()
                .addChanges(singletonList(changeObject))
                .build();
    }
}
