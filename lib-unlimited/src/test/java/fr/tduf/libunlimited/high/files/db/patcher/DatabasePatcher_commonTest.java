package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
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

    // TODO create method in database helper and use it (topic as arg)
    static DbDto readBotsObject() throws URISyntaxException, IOException {
        return DatabaseHelper.createDatabaseForReadOnly().stream()
                .filter(databaseObject -> BOTS == databaseObject.getStructure().getTopic())
                .findAny().get();
    }

    static DbDto readAchievementsObject() throws URISyntaxException, IOException {
        return DatabaseHelper.createDatabaseForReadOnly().stream()
                .filter(databaseObject -> ACHIEVEMENTS == databaseObject.getStructure().getTopic())
                .findAny().get();
    }

    static DbDto readCarColorsObject() throws URISyntaxException, IOException {
        return DatabaseHelper.createDatabaseForReadOnly().stream()
                .filter(databaseObject -> CAR_COLORS == databaseObject.getStructure().getTopic())
                .findAny().get();
    }

    static DbDto readCarPhysicsDataObject() throws URISyntaxException, IOException {
        return DatabaseHelper.createDatabaseForReadOnly().stream()
                .filter(databaseObject -> CAR_PHYSICS_DATA == databaseObject.getStructure().getTopic())
                .findAny().get();
    }

    static DbDto readCarShopsObject() throws URISyntaxException, IOException {
        return DatabaseHelper.createDatabaseForReadOnly().stream()
                .filter(databaseObject -> CAR_SHOPS == databaseObject.getStructure().getTopic())
                .findAny().get();
    }

    private static List<DbDto> createDatabaseObjects() {
        return singletonList(DbDto.builder().build());
    }
}
