package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.Collections.singletonList;

public class DatabasePatcher_commonTest {
    private static final Class<DatabasePatcher_commonTest> thisClass = DatabasePatcher_commonTest.class;

    private DatabasePatcher patcher;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        patcher = createPatcher(createDatabaseObjects());
    }

    @Test(expected = NullPointerException.class)
    public void apply_whenNullPatchObject_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN-WHEN
        patcher.apply(null);

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void batchApply_whenNullPatchList_shouldThrowException() throws ReflectiveOperationException, IOException, URISyntaxException {
        // GIVEN-WHEN
        patcher.batchApply(null);

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void batchApplyWithProperties_whenNullPatchMap_shouldThrowException() throws ReflectiveOperationException, IOException, URISyntaxException {
        // GIVEN-WHEN
        patcher.batchApplyWithProperties(null);

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void applyWithProperties_whenNullProperties_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN-WHEN
        patcher.applyWithProperties(DbPatchDto.builder().build(), null);

        // THEN: NPE
    }

    static DatabasePatcher createPatcher(List<DbDto> databaseObjects) throws ReflectiveOperationException {
        return AbstractDatabaseHolder.prepare(DatabasePatcher.class, databaseObjects);
    }

    static <T> T readObjectFromResource(Class<T> objectClass, String resource) throws URISyntaxException, IOException {
        URI resourceURI = thisClass.getResource(resource).toURI();
        return new ObjectMapper().readValue(new File(resourceURI), objectClass);
    }

    static DbDto readTopicObjectFromResources(String dataResource, String structureResource, String resourceResource) throws URISyntaxException, IOException {
        URI dataResourceURI = thisClass.getResource(dataResource).toURI();
        URI structureResourceURI = thisClass.getResource(structureResource).toURI();
        URI resourceResourceURI = thisClass.getResource(resourceResource).toURI();

        final ObjectMapper objectMapper = new ObjectMapper();

        return DbDto.builder()
                .withData(objectMapper.readValue(new File(dataResourceURI), DbDataDto.class))
                .withResource(objectMapper.readValue(new File(resourceResourceURI), DbResourceDto.class))
                .withStructure(objectMapper.readValue(new File(structureResourceURI), DbStructureDto.class))
                .build();
    }

    static DbDto readBotsObject() throws URISyntaxException, IOException {
        return readTopicObjectFromResources(
                "/db/json/TDU_Bots.data.json",
                "/db/json/TDU_Bots.structure.json",
                "/db/json/TDU_Bots.resources.json");
    }

    static DbDto readAchievementsObject() throws URISyntaxException, IOException {
        return readTopicObjectFromResources(
                "/db/json/TDU_Achievements.data.json",
                "/db/json/TDU_Achievements.structure.json",
                "/db/json/TDU_Achievements.resources.json");
    }

    static DbDto readCarColorsObject() throws URISyntaxException, IOException {
        return readTopicObjectFromResources(
                "/db/json/TDU_CarColors.data.json",
                "/db/json/TDU_CarColors.structure.json",
                "/db/json/TDU_CarColors.resources.json");
    }

    static DbDto readCarPacksObject() throws URISyntaxException, IOException {
        return readTopicObjectFromResources(
                "/db/json/TDU_CarPacks.data.json",
                "/db/json/TDU_CarPacks.structure.json",
                "/db/json/TDU_CarPacks.resources.json");
    }

    static DbDto readRimsObject() throws URISyntaxException, IOException {
        return readTopicObjectFromResources(
                "/db/json/TDU_Rims.data.json",
                "/db/json/TDU_Rims.structure.json",
                "/db/json/TDU_Rims.resources.json");
    }

    static DbDto readCarRimsObject() throws URISyntaxException, IOException {
        return readTopicObjectFromResources(
                "/db/json/TDU_CarRims.data.json",
                "/db/json/TDU_CarRims.structure.json",
                "/db/json/TDU_CarRims.resources.json");
    }

    static DbDto readCarPhysicsDataObject() throws URISyntaxException, IOException {
        return readTopicObjectFromResources(
                "/db/json/TDU_CarPhysicsData.data.json",
                "/db/json/TDU_CarPhysicsData.structure.json",
                "/db/json/TDU_CarPhysicsData.resources.json");
    }

    static DbDto readCarShopsObject() throws URISyntaxException, IOException {
        return readTopicObjectFromResources(
                "/db/json/TDU_CarShops.data.json",
                "/db/json/TDU_CarShops.structure.json",
                "/db/json/TDU_CarShops.resources.json");
    }

    static DbDto readBrandsObject() throws URISyntaxException, IOException {
        return readTopicObjectFromResources(
                "/db/json/TDU_Brands.data.json",
                "/db/json/TDU_Brands.structure.json",
                "/db/json/TDU_Brands.resources.json");
    }

    private static List<DbDto> createDatabaseObjects() {
        return singletonList(DbDto.builder().build());
    }
}
