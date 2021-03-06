package fr.tduf.libunlimited.high.files.db.patcher.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class DatabasePlaceholderResolverTest {

    @Mock
    private BulkDatabaseMiner minerMock;

    private DbDto databaseObject;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        initMocks(this);
        
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
    void resolveValuePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveValuePlaceholder("FOO", new DatabasePatchProperties(), null)
        ).isEqualTo("FOO");
    }

    @Test
    void resolveValuePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveValuePlaceholder("{FOO}", patchProperties, null)
        ).isEqualTo("1");
    }

    @Test
    void resolveValuePlaceholder_whenPlaceholder_withoutProperty_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> DatabasePlaceholderResolver.resolveValuePlaceholder("{FOO}", new DatabasePatchProperties(), null));
    }

    @Test
    void resolveValuePlaceholder_whenCARID_withoutProperty_shouldGenerateUniqueIdentifier() {
        // GIVEN
        when(minerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(Optional.of(databaseObject));

        // WHEN
        final String actual = DatabasePlaceholderResolver.resolveValuePlaceholder("{CARID}", new DatabasePatchProperties(), minerMock);

        // THEN
        int intValue = Integer.valueOf(actual);
        assertThat(intValue).isBetween(8000, 8999);
    }

    @Test
    void resolveContentsReferencePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveReferencePlaceholder(true, "FOO", new DatabasePatchProperties(), databaseObject, new HashSet<>())
        ).isEqualTo("FOO");
    }

    @Test
    void resolveContentsReferencePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveReferencePlaceholder(true, "{FOO}", patchProperties, databaseObject, new HashSet<>())
        ).isEqualTo("1");
    }

    @Test
    void resolveContentsReferencePlaceholder_whenPlaceholder_andPseudoRef_withProperties_shouldReturnPropertyValue() {
        // GIVEN
        final DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register("FOO1", "1");
        patchProperties.register("FOO2", "2");

        // WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveReferencePlaceholder(true, "{FOO1}|{FOO2}", patchProperties, databaseObject, new HashSet<>())
        ).isEqualTo("1|2");
    }

    @Test
    void resolveContentsReferencePlaceholder_whenPlaceholder_andPseudoRef_withMissingProperty_shouldReturnPropertyValue() {
        // GIVEN
        final DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register("FOO1", "1");

        // WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveReferencePlaceholder(true, "{FOO1}|{FOO2}", patchProperties, databaseObject, new HashSet<>())
        ).startsWith("1|");
    }

    @Test
    void resolveContentsReferencePlaceholder_whenPlaceholder_withMissingProperty_shouldReturnProvidedAndGeneratedValues() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveReferencePlaceholder(true, "{FOO}", new DatabasePatchProperties(), databaseObject, new HashSet<>())
        ).isNotNull();
    }

    @Test
    void resolveResourceReferencePlaceholder_whenNoPlaceholder_shouldReturnInitialValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveReferencePlaceholder(false, "FOO", new DatabasePatchProperties(), databaseObject, new HashSet<>())
        ).isEqualTo("FOO");
    }

    @Test
    void resolveResourceReferencePlaceholder_whenPlaceholder_withProperty_shouldReturnPropertyValue() {
        // GIVEN
        final DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        patchProperties.register("FOO", "1");

        // WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveReferencePlaceholder(false, "{FOO}", patchProperties, databaseObject, new HashSet<>())
        ).isEqualTo("1");
    }

    @Test
    void resolveResourceReferencePlaceholder_whenPlaceholder_withoutProperty_shouldReturnGeneratedValue() {
        // GIVEN-WHEN-THEN
        assertThat(
                DatabasePlaceholderResolver.resolveReferencePlaceholder(false, "{FOO}", new DatabasePatchProperties(), databaseObject, new HashSet<>())
        ).isNotNull();
    }
}
