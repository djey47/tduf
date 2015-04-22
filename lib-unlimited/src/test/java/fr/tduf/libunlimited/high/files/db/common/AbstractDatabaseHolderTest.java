package fr.tduf.libunlimited.high.files.db.common;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class AbstractDatabaseHolderTest {

    @Test
    public void prepare_shouldSetDatabaseObject() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDefaultDatabaseObjects();

        // WHEN
        PoorDatabaseHolder holder = AbstractDatabaseHolder.prepare(PoorDatabaseHolder.class, databaseObjects);

        // THEN
        assertThat(holder).isNotNull();
        assertThat(holder.getDatabaseObjects()).isSameAs(databaseObjects);
    }

    @Test(expected = NullPointerException.class)
    public void prepare_whenNullDatabaseObject_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN-WHEN
        AbstractDatabaseHolder.prepare(PoorDatabaseHolder.class, null);

        // THEN: NPPE
    }

    static class PoorDatabaseHolder extends AbstractDatabaseHolder {}

    private static List<DbDto> createDefaultDatabaseObjects() {
        return singletonList(DbDto.builder().build());
    }
}