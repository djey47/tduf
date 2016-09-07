package fr.tduf.gui.database.common.helper;

import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.framework.primitives.Ints.asList;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.REFERENCE;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.RESOURCE_CURRENT_GLOBALIZED;
import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.RESOURCE_CURRENT_LOCALIZED;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseQueryHelperTest {
    private static final DbDto.Topic TOPIC = CAR_PHYSICS_DATA;
    private static final DbDto.Topic TOPIC_REMOTE = BRANDS;
    private static final String REF_REMOTE_TOPIC = "123";

    private EditorLayoutDto layoutObject = loadProfiles();

    @Mock
    private BulkDatabaseMiner minerMock;

    public DatabaseQueryHelperTest() throws IOException, URISyntaxException {
    }

    @Before
    public void setUp() {
        when(minerMock.getDatabaseTopic(TOPIC)).thenReturn(of(createDatabaseObject()));

        final DbDto remoteDatabaseObject = createRemoteDatabaseObject();
        when(minerMock.getDatabaseTopic(TOPIC_REMOTE)).thenReturn(of(remoteDatabaseObject));
        when(minerMock.getDatabaseTopicFromReference(REF_REMOTE_TOPIC)).thenReturn(remoteDatabaseObject);
    }

    @Test
    public void fetchResourceValuesWithEntryId_whenNoFieldRanks_shouldReturnUnavailable() throws Exception {
        // GIVEN - WHEN
        final String actualLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, TOPIC, FRANCE, new ArrayList<>(), minerMock, layoutObject);

        // THEN
        assertThat(actualLabel).isEqualTo("<?>");
    }

    @Test
    public void fetchResourceValuesWithEntryId_whenLocalResourcesAvailable() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = asList(3, 4);
        String resValue1 = "RES1";
        String resValue2 = "RES2";

        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 3, TOPIC, FRANCE)).thenReturn(of(resValue1));
        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 4, TOPIC,  FRANCE)).thenReturn(of(resValue2));


        // WHEN
        final String actualLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, TOPIC, FRANCE, fieldRanks, minerMock, layoutObject);


        // THEN
        assertThat(actualLabel).isEqualTo("RES1 - RES2");
    }

    @Test
    public void fetchResourceValuesWithEntryId_whenLocalResourceUnavailable_shouldReturnItemRawValue() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = asList(3, 4);
        String resValue1 = "RES1";
        ContentItemDto item2 = ContentItemDto.builder()
                .ofFieldRank(4)
                .withRawValue("85467580")
                .build();

        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 3, TOPIC, FRANCE)).thenReturn(of(resValue1));
        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 4, TOPIC, FRANCE)).thenReturn(empty());
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(TOPIC, 4, 1)).thenReturn(of(item2));


        // WHEN
        final String actualLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, TOPIC, FRANCE, fieldRanks, minerMock, layoutObject);


        // THEN
        assertThat(actualLabel).isEqualTo("RES1 - <85467580>");
    }

    @Test
    public void fetchResourceValuesWithEntryId_whenReferenceField_andRemoteResourceAvailable() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = asList(4, 5);
        String resValue1 = "RES1";
        String resValue2 = "REMOTE ENTRY";

        ContentEntryDto remoteEntry = createRemoteContentEntry();
        when(minerMock.getRemoteContentEntryWithInternalIdentifier(TOPIC, 5, 1, TOPIC_REMOTE)).thenReturn(of(remoteEntry));
        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 4, TOPIC, FRANCE)).thenReturn(of(resValue1));
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC_REMOTE, FRANCE)).thenReturn(of(resValue2));

        // WHEN
        final String actualLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, TOPIC, FRANCE, fieldRanks, minerMock, layoutObject);


        // THEN
        assertThat(actualLabel).isEqualTo("RES1 - REMOTE ENTRY");
    }

    @Test(expected = IllegalStateException.class)
    public void fetchResourceValuesWithEntryId_whenLocalResourceUnavailable_andItemUnavailable_shouldThrowException() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = singletonList(3);

        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 3, TOPIC, FRANCE)).thenReturn(empty());
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(TOPIC, 3, 1)).thenReturn(empty());


        // WHEN
        DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, TOPIC, FRANCE, fieldRanks, minerMock, layoutObject);


        // THEN: ISE
    }

    @Test
    public void fetchResourceValuesWithEntryId_whenReferenceField_andRemoteResourceUnavailable_shouldWriteRawValue() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = asList(4, 5);
        String resValue1 = "RES1";

        ContentEntryDto remoteEntry = createRemoteContentEntry();
        ContentItemDto remoteItem = ContentItemDto.builder().ofFieldRank(1).withRawValue("RAW").build();
        remoteEntry.appendItem(remoteItem);

        when(minerMock.getRemoteContentEntryWithInternalIdentifier(TOPIC, 5, 1, TOPIC_REMOTE)).thenReturn(of(remoteEntry));
        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 4, TOPIC, FRANCE)).thenReturn(of(resValue1));
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC_REMOTE, FRANCE)).thenReturn(empty());
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(TOPIC_REMOTE, 1, 0)).thenReturn(of(remoteItem));


        // WHEN
        final String actualLabel = DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, TOPIC, FRANCE, fieldRanks, minerMock, layoutObject);


        // THEN
        assertThat(actualLabel).isEqualTo("RES1 - <RAW>");
    }

    private ContentEntryDto createRemoteContentEntry() {
        ContentEntryDto remoteEntry = ContentEntryDto.builder().build();
        DbDataDto.builder().addEntry(remoteEntry).build();
        return remoteEntry;
    }

    @Test(expected = IllegalStateException.class)
    public void fetchResourceValuesWithEntryId_whenReferenceField_andRemoteResourceUnavailable_andItemUnavailable_shouldWriteRawValue() throws Exception {
        // GIVEN
        List<Integer> fieldRanks = asList(4, 5);
        String resValue1 = "RES1";

        ContentEntryDto remoteEntry = createRemoteContentEntry();
        ContentItemDto remoteItem = ContentItemDto.builder().ofFieldRank(1).withRawValue("RAW").build();
        remoteEntry.appendItem(remoteItem);

        when(minerMock.getRemoteContentEntryWithInternalIdentifier(TOPIC, 5, 1, TOPIC_REMOTE)).thenReturn(of(remoteEntry));
        when(minerMock.getLocalizedResourceValueFromContentEntry(1, 4, TOPIC, FRANCE)).thenReturn(of(resValue1));
        when(minerMock.getLocalizedResourceValueFromContentEntry(0, 1, TOPIC_REMOTE, FRANCE)).thenReturn(empty());
        when(minerMock.getContentItemWithEntryIdentifierAndFieldRank(TOPIC_REMOTE, 1, 0)).thenReturn(empty());


        // WHEN
        DatabaseQueryHelper.fetchResourceValuesWithEntryId(1, TOPIC, FRANCE, fieldRanks, minerMock, layoutObject);


        // THEN: ISE
    }

    private DbDto createDatabaseObject() {
        DbStructureDto structureObject = DbStructureDto.builder()
                .forTopic(TOPIC)
                .addItem(DbStructureDto.Field.builder().ofRank(1).fromType(RESOURCE_CURRENT_LOCALIZED).build())
                .addItem(DbStructureDto.Field.builder().ofRank(2).fromType(RESOURCE_CURRENT_LOCALIZED).build())
                .addItem(DbStructureDto.Field.builder().ofRank(3).fromType(RESOURCE_CURRENT_LOCALIZED).build())
                .addItem(DbStructureDto.Field.builder().ofRank(4).fromType(RESOURCE_CURRENT_GLOBALIZED).build())
                .addItem(DbStructureDto.Field.builder().ofRank(5).fromType(REFERENCE).toTargetReference(REF_REMOTE_TOPIC).build())
                .build();
        return DbDto.builder()
                .withStructure(structureObject)
                .build();
    }

    private DbDto createRemoteDatabaseObject() {
        DbStructureDto structureObject = DbStructureDto.builder()
                .forTopic(TOPIC_REMOTE)
                .addItem(DbStructureDto.Field.builder().ofRank(1).fromType(RESOURCE_CURRENT_LOCALIZED).build())
                .build();
        return DbDto.builder()
                .withStructure(structureObject)
                .build();
    }

    private EditorLayoutDto loadProfiles() throws IOException, URISyntaxException {
        return FilesHelper.readObjectFromJsonResourceFile(EditorLayoutDto.class, "/layout/testProfiles.json");
    }
}
