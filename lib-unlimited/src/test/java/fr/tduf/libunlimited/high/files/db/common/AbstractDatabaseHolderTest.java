package fr.tduf.libunlimited.high.files.db.common;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractDatabaseHolderTest {

    @Test
    void prepare_shouldSetDatabaseObject_andExecutePostActions() throws ReflectiveOperationException {
        // GIVEN
        List<DbDto> databaseObjects = createDefaultDatabaseObjects();

        // WHEN
        PoorDatabaseHolder holder = AbstractDatabaseHolder.prepare(PoorDatabaseHolder.class, databaseObjects);

        // THEN
        assertThat(holder).isNotNull();
        assertThat(holder.getDatabaseObjects()).isSameAs(databaseObjects);
        assertThat(holder.getI()).isEqualTo(1);
    }

    @Test
    void prepare_whenNullDatabaseObject_shouldThrowException() throws ReflectiveOperationException {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> AbstractDatabaseHolder.prepare(PoorDatabaseHolder.class, null));
    }

    static class PoorDatabaseHolder extends AbstractDatabaseHolder {

        private int i = 0;

        @Override
        protected void postPrepare() {
            i++;
        }

        int getI() {
            return i;
        }
    }

    private static List<DbDto> createDefaultDatabaseObjects() {
        return singletonList(DbDto.builder().build());
    }
}