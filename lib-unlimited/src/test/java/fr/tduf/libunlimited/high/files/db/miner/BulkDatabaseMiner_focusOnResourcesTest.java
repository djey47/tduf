package fr.tduf.libunlimited.high.files.db.miner;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceEnhancedDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.PNJ;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class BulkDatabaseMiner_focusOnResourcesTest {

    private static final DbDto.Topic TOPIC = CAR_PHYSICS_DATA;
    private static final String RESOURCE_REF = "00000000";

    @Test
    public void getResourceEnhancedFromTopic_whenTopicExists_shouldReturnIt() {
        //GIVEN
        DbDto topicObject1 = createDefaultTopicObject(TOPIC);
        DbDto topicObject2 = createDefaultTopicObject(PNJ);
        List<DbDto> topicObjects = asList(topicObject1, topicObject2);

        //WHEN
        final Optional<DbResourceEnhancedDto> potentialResource = BulkDatabaseMiner.load(topicObjects).getResourceEnhancedFromTopic(TOPIC);

        //THEN
        assertThat(potentialResource)
                .isPresent()
                .contains(topicObject1.getResource());
    }

    @Test
    public void getResourceEnhancedFromTopic_whenTopicDoesNotExist_shouldReturnEmpty() {
        //GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        //WHEN
        final Optional<DbResourceEnhancedDto> potentialResource = BulkDatabaseMiner.load(topicObjects).getResourceEnhancedFromTopic(PNJ);

        //THEN
        assertThat(potentialResource).isEmpty();
    }

    @Test
    public void getResourceEntryFromTopicAndReference_whenTopicDoesNotExist_shouldReturnEmpty() {
        //GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        //WHEN
        final Optional<DbResourceEnhancedDto.Entry> potentialEntry = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndReference(PNJ, RESOURCE_REF);

        //THEN
        assertThat(potentialEntry).isEmpty();
    }

    @Test
    public void getResourceEntryFromTopicAndReference_whenTopicExists_butEntryDoesNot_shouldReturnEmpty() {
        //GIVEN
        List<DbDto> topicObjects = singletonList(createDefaultTopicObject(TOPIC));

        //WHEN
        final Optional<DbResourceEnhancedDto.Entry> potentialEntry = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REF);

        //THEN
        assertThat(potentialEntry).isEmpty();
    }

    @Test
    public void getResourceEntryFromTopicAndReference_whenEntryExists_shouldReturnIt() {
        //GIVEN
        final DbDto topicObject = createDefaultTopicObject(TOPIC);
        final DbResourceEnhancedDto.Entry entry = topicObject.getResource().addEntryByReference(RESOURCE_REF);
        List<DbDto> topicObjects = singletonList(topicObject);

        //WHEN
        final Optional<DbResourceEnhancedDto.Entry> potentialEntry = BulkDatabaseMiner.load(topicObjects).getResourceEntryFromTopicAndReference(TOPIC, RESOURCE_REF);

        //THEN
        assertThat(potentialEntry)
                .isPresent()
                .contains(entry);
    }

    private static DbDto createDefaultTopicObject(DbDto.Topic topic) {
        DbResourceEnhancedDto resourceObject = DbResourceEnhancedDto.builder()
                .withCategoryCount(1)
                .atVersion("1,0")
                .build();
        DbStructureDto structureObject = DbStructureDto.builder()
                .forTopic(topic)
                .build();
        return DbDto.builder()
                .withStructure(structureObject)
                .withResource(resourceObject)
                .build();
    }
}
