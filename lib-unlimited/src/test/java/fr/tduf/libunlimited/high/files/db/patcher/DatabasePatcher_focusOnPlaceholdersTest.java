package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.FRANCE;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.UID;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

// TODO extending another test case also runs those cases...
public class DatabasePatcher_focusOnPlaceholdersTest extends DatabasePatcher_commonTest {

    private DbDto databaseObject;
    private DatabasePatcher databasePatcher;

    @Before
    public void setUp() throws ReflectiveOperationException {
        super.setUp();

        databaseObject = DbDto.builder()
                .withData(DbDataDto.builder().build())
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
                .addResource(DbResourceDto.builder().withLocale(FRANCE).build())
                .build();
        databasePatcher = createPatcher(Collections.singletonList(databaseObject));
    }

    @Test
    public void resolvePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePatcher.resolvePlaceholder("FOO", new PatchProperties(), empty())
        ).isEqualTo("FOO");
    }
    @Test
    public void resolvePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                DatabasePatcher.resolvePlaceholder("{FOO}", patchProperties, empty())
        ).isEqualTo("1");
    }

    @Test
    public void resolvePlaceholder_whenPlaceholder_withoutProperty_shouldReturnGeneratedValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePatcher.resolvePlaceholder("{FOO}", new PatchProperties(), of(databaseObject))
        ).isNotNull();
    }
    @Test
    public void resolveResourcePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePatcher.resolveResourcePlaceholder("FOO", new PatchProperties(), empty())
        ).isEqualTo("FOO");
    }

    @Test
    public void resolveResourcePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                DatabasePatcher.resolveResourcePlaceholder("{FOO}", patchProperties, empty())
        ).isEqualTo("1");
    }

    @Test
    public void resolveResourcePlaceholder_whenPlaceholder_withoutProperty_shouldReturnGeneratedValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePatcher.resolveResourcePlaceholder("{FOO}", new PatchProperties(), of(databaseObject))
        ).isNotNull();
    }

    @Test
    public void apply_whenUpdateContents_forRef_withProperty_shouldUsePropertyValue() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName = "MYREF";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(UPDATE)
                .forTopic(CAR_PHYSICS_DATA)
                .asReferencePlaceholder(placeholderName)
                .withEntryValues(asList("{MYREF}", "103"))
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);
        PatchProperties patchProperties = new PatchProperties();
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
    public void apply_whenUpdateContents_forRef_withoutProperty_shouldUseGeneratedValue() {
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
        final PatchProperties actualProperties = databasePatcher.applyWithProperties(patchObject, new PatchProperties());


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
    public void apply_whenDeleteContents_forRef_withProperty_shouldUsePropertyValue() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName = "MYREF";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(DELETE)
                .forTopic(CAR_PHYSICS_DATA)
                .asReferencePlaceholder(placeholderName)
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.register(placeholderName, "000000");

        databaseObject.getData().addEntryWithItems(asList(
                DbDataDto.Item.builder()
                        .ofFieldRank(1)
                        .withRawValue("000000")
                        .build(),
                DbDataDto.Item.builder()
                        .ofFieldRank(2)
                        .build()));


        // WHEN
        databasePatcher.applyWithProperties(patchObject, patchProperties);


        // THEN
        assertThat(databaseObject.getData().getEntries()).isEmpty();
    }

    @Test
    public void apply_whenUpdateResources_forRef_withProperties_shouldUsePropertiesValues() throws ReflectiveOperationException {
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
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.register(placeholderName1, "000000");
        patchProperties.register(placeholderName2, "Text");


        // WHEN
        databasePatcher.applyWithProperties(patchObject, patchProperties);


        // THEN
        assertThat(databaseObject.getResources().get(0).getEntries()).hasSize(1);

        final DbResourceDto.Entry resourceEntry = databaseObject.getResources().get(0).getEntries().get(0);
        assertThat(resourceEntry.getReference()).isEqualTo("000000");
        assertThat(resourceEntry.getValue()).isEqualTo("Text");
    }

    @Test
    public void apply_whenUpdateResources_forRef_withoutProperty_shouldUseGeneratedValue() throws ReflectiveOperationException {
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
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.register(placeholderName2, "Text");


        // WHEN
        final PatchProperties actualProperties = databasePatcher.applyWithProperties(patchObject, patchProperties);


        // THEN
        assertThat(actualProperties.size()).isEqualTo(2);
        assertThat(databaseObject.getResources().get(0).getEntries()).hasSize(1);

        final String generatedValue = actualProperties.getProperty("MYRESREF");
        final DbResourceDto.Entry resourceEntry = databaseObject.getResources().get(0).getEntries().get(0);
        assertThat(resourceEntry.getReference()).isEqualTo(generatedValue);
        assertThat(resourceEntry.getValue()).isEqualTo("Text");
    }

    @Test
    public void apply_whenDeleteResources_forRef_withProperty_shouldUsePropertyValue() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName = "MYREF";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(DELETE_RES)
                .forTopic(CAR_PHYSICS_DATA)
                .asReferencePlaceholder(placeholderName)
                .build();
        DbPatchDto patchObject = createPatchObjectWithSingleChange(changeObject);
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.register(placeholderName, "000000");

        databaseObject.getResources().get(0).getEntries().add(
                DbResourceDto.Entry.builder()
                        .forReference("000000")
                        .withValue("Text")
                        .build());


        // WHEN
        databasePatcher.applyWithProperties(patchObject, patchProperties);


        // THEN
        assertThat(databaseObject.getResources().get(0).getEntries()).isEmpty();
    }

    private static DbPatchDto createPatchObjectWithSingleChange(DbPatchDto.DbChangeDto changeObject) {
        return DbPatchDto.builder()
                .addChanges(Collections.singletonList(changeObject))
                .build();
    }
}
