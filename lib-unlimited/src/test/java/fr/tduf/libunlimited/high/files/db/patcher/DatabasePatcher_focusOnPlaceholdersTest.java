package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
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
                        .forTopic(DbDto.Topic.CAR_PHYSICS_DATA)
                        .addItem(DbStructureDto.Field.builder().ofRank(1).build())
                        .addItem(DbStructureDto.Field.builder().ofRank(2).build())
                        .build())
                .build();
        databasePatcher =  createPatcher(Collections.singletonList(databaseObject));
    }

    @Test
    public void resolvePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePatcher.resolvePlaceholder("FOO", new PatchProperties())
        ).isEqualTo("FOO");
    }

    @Test
    public void resolvePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final PatchProperties patchProperties = new PatchProperties();
        patchProperties.store("FOO", "1");

        // WHEN-THEN
        assertThat(
                DatabasePatcher.resolvePlaceholder("{FOO}", patchProperties)
        ).isEqualTo("1");
    }

    @Test
    public void apply_whenUpdateContents_forRef_withProperty_shouldUsePropertyValue() throws ReflectiveOperationException {
        // GIVEN
        final String placeholderName = "MYREF";
        DbPatchDto.DbChangeDto changeObject = DbPatchDto.DbChangeDto.builder()
                .withType(DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE)
                .forTopic(DbDto.Topic.CAR_PHYSICS_DATA)
                .asReferencePlaceholder(placeholderName)
                .withEntryValues(asList("{MYREF}", "103"))
                .build();
        DbPatchDto patchObject = DbPatchDto.builder()
                .addChanges(Collections.singletonList(changeObject))
                .build();
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.store(placeholderName, "000000");

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

    }
}
