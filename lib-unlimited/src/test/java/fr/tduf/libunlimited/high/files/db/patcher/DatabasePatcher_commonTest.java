package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
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

    @Before
    public void setUp() {}

    @Test(expected = NullPointerException.class)
    public void apply_whenNullPatchObject_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN-WHEN
        createPatcher(createDefaultDatabaseObjects()).apply(null);

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void batchApply_whenNullPatchList_shouldThrowException() throws ReflectiveOperationException, IOException, URISyntaxException {
        // GIVEN
        DbDto databaseObject = readObjectFromResource(DbDto.class, "/db/json/TDU_Bots.json");
        DatabasePatcher patcher = createPatcher(singletonList(databaseObject));

        // WHEN
        patcher.batchApply(null);

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void applyWithProperties_whenNullProperties_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN-WHEN
        createPatcher(createDefaultDatabaseObjects()).applyWithProperties(DbPatchDto.builder().build(), null);

        // THEN: NPE
    }

    static DatabasePatcher createPatcher(List<DbDto> databaseObjects) throws ReflectiveOperationException {
        return AbstractDatabaseHolder.prepare(DatabasePatcher.class, databaseObjects);
    }

    static <T> T readObjectFromResource(Class<T> objectClass, String resource) throws URISyntaxException, IOException {
        URI resourceURI = thisClass.getResource(resource).toURI();
        return new ObjectMapper().readValue(new File(resourceURI), objectClass);
    }

    private static List<DbDto> createDefaultDatabaseObjects() {
        return singletonList(DbDto.builder().build());
    }
}
