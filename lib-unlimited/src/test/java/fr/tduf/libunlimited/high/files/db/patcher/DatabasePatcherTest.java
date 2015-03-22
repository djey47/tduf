package fr.tduf.libunlimited.high.files.db.patcher;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DatabasePatcherTest {

    @Test(expected = NullPointerException.class)
    public void prepare_whenNullDatabaseObject_shouldThrowException() {
        // GIVEN-WHEN
        DatabasePatcher.prepare(null);

        // THEN: NPE
    }

    @Test
    public void prepare_shouldSetDatabaseObject() {
        // GIVEN
        DbDto databaseObject = DbDto.builder().build();

        // WHEN
        DatabasePatcher patcher = DatabasePatcher.prepare(databaseObject);

        // THEN
        assertThat(patcher).isNotNull();
        assertThat(patcher.getDatabaseObject()).isSameAs(databaseObject);
    }

}