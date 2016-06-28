package fr.tduf.libunlimited.low.files.db.rw.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class DatabaseStructureQueryHelperTest {

    @Test(expected = NullPointerException.class)
    public void getUidField_whenFieldListNull_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        assertThat(DatabaseStructureQueryHelper.getUidField(null)).isNull();

        // THEN: NPE
    }

    @Test
    public void getUidField_whenNoIdentifierField_shouldReturnAbsent() throws Exception {
        // GIVEN
        DbStructureDto structureObject = DbStructureDto.builder()
                .addItem(createClassicField())
                .build();

        // WHEN-THEN
        assertThat(DatabaseStructureQueryHelper.getUidField(structureObject.getFields())).isEmpty();
    }

    @Test
    public void getUidField_whenFound_shouldReturnIt() throws Exception {
        // GIVEN
        DbStructureDto.Field identifierField = createIdentifierField();
        DbStructureDto structureObject = createStructureWithTwoFields(identifierField);

        // WHEN
        Optional<DbStructureDto.Field> actualField = DatabaseStructureQueryHelper.getUidField(structureObject.getFields());

        // THEN
        assertThat(actualField).contains(identifierField);
    }

    @Test(expected = NullPointerException.class)
    public void getStructureField_whenNullArguments_shouldThrowException() {
        // GIVEN-WHEN
        DatabaseStructureQueryHelper.getStructureField(null, null);

        // THEN: NPE
    }

    @Test(expected = IllegalStateException.class)
    public void getStructureField_whenNotFound_shouldThrowException() throws Exception {
        // GIVEN
        ContentItemDto contentsItem = ContentItemDto.builder()
                .ofFieldRank(2)
                .build();
        DbStructureDto structureObject = createStructureWithTwoFields(createIdentifierField());

        // WHEN
        DatabaseStructureQueryHelper.getStructureField(contentsItem, structureObject.getFields());

        // THEN: NSEE
    }

    @Test
    public void getStructureField_whenFound_shouldReturnIt() throws Exception {
        // GIVEN
        ContentItemDto contentsItem = ContentItemDto.builder()
                .ofFieldRank(0)
                .build();
        DbStructureDto.Field identifierField = createIdentifierField();
        DbStructureDto structureObject = createStructureWithTwoFields(identifierField);

        // WHEN
        DbStructureDto.Field actualField = DatabaseStructureQueryHelper.getStructureField(contentsItem, structureObject.getFields());

        // THEN
        assertThat(actualField).isNotNull();
        assertThat(actualField).isEqualTo(identifierField);
    }

    @Test
    public void getUidFieldRank_whenNoIdentifierField_shouldReturnAbsent() {
        // GIVEN
        List<DbStructureDto.Field> structureFields = new ArrayList<>();

        // WHEN-THEN
        assertThat(DatabaseStructureQueryHelper.getUidFieldRank(structureFields).isPresent()).isFalse();
    }

    @Test
    public void getUidFieldRank_whenIdentifierFieldPresent_shouldReturnRank() throws IOException, URISyntaxException {
        // GIVEN
        List<DbStructureDto.Field> structureFields = createTopicObjectsFromResources().get(0).getStructure().getFields();

        // WHEN-THEN
        assertThat(DatabaseStructureQueryHelper.getUidFieldRank(structureFields).getAsInt()).isEqualTo(1);
    }

    private DbStructureDto createStructureWithTwoFields(DbStructureDto.Field identifierField) {
        return DbStructureDto.builder()
                .addItem(identifierField)
                .addItem(createClassicField())
                .build();
    }

    private static DbStructureDto.Field createIdentifierField() {
        return DbStructureDto.Field.builder()
                .ofRank(0)
                .fromType(DbStructureDto.FieldType.UID)
                .build();
    }

    private static DbStructureDto.Field createClassicField() {
        return DbStructureDto.Field.builder()
                .ofRank(1)
                .fromType(DbStructureDto.FieldType.INTEGER)
                .build();
    }

    private static ArrayList<DbDto> createTopicObjectsFromResources() throws IOException, URISyntaxException {
        ArrayList<DbDto> dbDtos = new ArrayList<>();

        dbDtos.add(FilesHelper.readObjectFromJsonResourceFile(DbDto.class, "/db/json/miner/TDU_Bots_FAKE.json"));

        return dbDtos;
    }
}