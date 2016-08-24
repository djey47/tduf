package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.Collections.singletonList;

public class DatabasePatcher_commonTest {

    private DatabasePatcher patcher;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        patcher = createPatcher(createEmptyDatabaseObjects());
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

    private static List<DbDto> createEmptyDatabaseObjects() {
        return singletonList(DbDto.builder().build());
    }
}
