package fr.tduf.libunlimited.low.files.db.writer;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DbWriterTest {

    @Test
    public void load_whenProvidedContents_shouldReturnWriterInstance() throws Exception {
        //GIVEN
        DbDto dbDto = DbDto.builder().build();

        //WHEN
        DbWriter dbWriter = DbWriter.load(dbDto);

        //THEN
        assertThat(dbWriter).isNotNull();
    }
}