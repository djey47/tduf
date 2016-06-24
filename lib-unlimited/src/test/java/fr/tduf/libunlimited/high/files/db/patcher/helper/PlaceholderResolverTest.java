package fr.tduf.libunlimited.high.files.db.patcher.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PlaceholderResolverTest {

    @Mock
    private BulkDatabaseMiner minerMock;

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
                        .addItem(DbStructureDto.Field.builder()
                                .ofRank(102)
                                .fromType(INTEGER)
                                .build())
                        .build())
                .withResource(DbResourceDto.builder()
                        .withCategoryCount(1)
                        .atVersion("1,0")
                        .build())
                .build();
    }

    @Test
    public void resolveValuePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveValuePlaceholder("FOO", new PatchProperties(), null)
        ).isEqualTo("FOO");
    }

    @Test
    public void resolveValuePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveValuePlaceholder("{FOO}", patchProperties, null)
        ).isEqualTo("1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void resolveValuePlaceholder_whenPlaceholder_withoutProperty_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveValuePlaceholder("{FOO}", new PatchProperties(), null)
        ).isEqualTo("1");
    }

    @Test
    public void resolveValuePlaceholder_whenCARID_withoutProperty_shouldGenerateUniqueIdentifier() {
        // GIVEN
        when(minerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(Optional.of(databaseObject));

        // WHEN
        final String actual = PlaceholderResolver.resolveValuePlaceholder("{CARID}", new PatchProperties(), minerMock);

        // THEN
        int intValue = Integer.valueOf(actual);
        assertThat(intValue).isBetween(8000, 8999);
    }

    @Test
    public void resolveContentsReferencePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveReferencePlaceholder(true, "FOO", new PatchProperties(), databaseObject, new HashSet<>())
        ).isEqualTo("FOO");
    }

    @Test
    public void resolveContentsReferencePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveReferencePlaceholder(true, "{FOO}", patchProperties, databaseObject, new HashSet<>())
        ).isEqualTo("1");
    }

    @Test
    public void resolveContentsReferencePlaceholder_whenPlaceholder_withoutProperty_shouldReturnGeneratedValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveReferencePlaceholder(true, "{FOO}", new PatchProperties(), databaseObject, new HashSet<>())
        ).isNotNull();
    }

    @Test
    public void resolveResourceReferencePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveReferencePlaceholder(false, "FOO", new PatchProperties(), databaseObject, new HashSet<>())
        ).isEqualTo("FOO");
    }

    @Test
    public void resolveResourceReferencePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final PatchProperties patchProperties = new PatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveReferencePlaceholder(false, "{FOO}", patchProperties, databaseObject, new HashSet<>())
        ).isEqualTo("1");
    }

    @Test
    public void resolveResourceReferencePlaceholder_whenPlaceholder_withoutProperty_shouldReturnGeneratedValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                PlaceholderResolver.resolveReferencePlaceholder(false, "{FOO}", new PatchProperties(), databaseObject, new HashSet<>())
        ).isNotNull();
    }
}
