package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.expectThrows;

class DatabasePatcher_commonTest {

    private static DatabasePatcher patcher;
    
    @BeforeAll
    static void setUp() throws ReflectiveOperationException {
        patcher = createPatcher(createEmptyDatabaseObjects());
    }

    @Test
     void apply_whenNullPatchObject_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN-WHEN-THEN
        expectThrows(NullPointerException.class,
                () -> patcher.apply(null));
    }

    @Test
     void batchApply_whenNullPatchList_shouldThrowException() throws ReflectiveOperationException, IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        expectThrows(NullPointerException.class,
                () -> patcher.batchApply(null));
    }

    @Test
     void batchApplyWithProperties_whenNullPatchMap_shouldThrowException() throws ReflectiveOperationException, IOException, URISyntaxException {
        // GIVEN-WHEN-THEN
        expectThrows(NullPointerException.class,
                () -> patcher.batchApplyWithProperties(null));
    }

    @Test
     void applyWithProperties_whenNullProperties_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN-WHEN-THEN
        expectThrows(NullPointerException.class,
                () -> patcher.applyWithProperties(DbPatchDto.builder().build(), null));
    }

    static DatabasePatcher createPatcher(List<DbDto> databaseObjects) throws ReflectiveOperationException {
        return AbstractDatabaseHolder.prepare(DatabasePatcher.class, databaseObjects);
    }

    private static List<DbDto> createEmptyDatabaseObjects() {
        return singletonList(DbDto.builder().build());
    }
}
