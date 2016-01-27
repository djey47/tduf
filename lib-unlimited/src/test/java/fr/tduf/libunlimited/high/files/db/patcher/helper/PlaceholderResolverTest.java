package fr.tduf.libunlimited.high.files.db.patcher.helper;

import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Before;
import org.junit.Test;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.FRANCE;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.UID;
import static org.assertj.core.api.Assertions.assertThat;

public class PlaceholderResolverTest {

    private DbDto databaseObject;

    @Before
    public void setUp() throws ReflectiveOperationException {
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
    }

    @Test
    public void resolveValuePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveValuePlaceholder("FOO", new PatchProperties())
        ).isEqualTo("FOO");
    }

    @Test
    public void resolveValuePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveValuePlaceholder("{FOO}", patchProperties)
        ).isEqualTo("1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void resolveValuePlaceholder_whenPlaceholder_withoutProperty_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveValuePlaceholder("{FOO}", new PatchProperties())
        ).isEqualTo("1");
    }

    @Test
    public void resolveContentsReferencePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveContentsReferencePlaceholder("FOO", new PatchProperties(), databaseObject)
        ).isEqualTo("FOO");
    }
    @Test
    public void resolveContentsReferencePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveContentsReferencePlaceholder("{FOO}", patchProperties, databaseObject)
        ).isEqualTo("1");
    }

    @Test
    public void resolveContentsReferencePlaceholder_whenPlaceholder_withoutProperty_shouldReturnGeneratedValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveContentsReferencePlaceholder("{FOO}", new PatchProperties(), databaseObject)
        ).isNotNull();
    }

    @Test
    public void resolveResourceReferencePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveResourceReferencePlaceholder("FOO", new PatchProperties(), databaseObject)
        ).isEqualTo("FOO");
    }

    @Test
    public void resolveResourceReferencePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveResourceReferencePlaceholder("{FOO}", patchProperties, databaseObject)
        ).isEqualTo("1");
    }

    @Test
    public void resolveResourceReferencePlaceholder_whenPlaceholder_withoutProperty_shouldReturnGeneratedValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveResourceReferencePlaceholder("{FOO}", new PatchProperties(), databaseObject)
        ).isNotNull();
    }
}
