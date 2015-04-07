package fr.tduf.gui.database.helper;


import fr.tduf.gui.database.dto.EditorLayoutDto;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class EditorLayoutHelperTest {

    @Test(expected = NullPointerException.class)
    public void getAvailableProfileByName_whenLayoutObjectNull_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        EditorLayoutHelper.getAvailableProfileByName(null, null);

        // THEN: NPE
    }

    @Test(expected = NoSuchElementException.class)
    public void getAvailableProfileByName_whenProfileNotFound_shouldThrowException() {
        // GIVEN-WHEN
        EditorLayoutHelper.getAvailableProfileByName("", new EditorLayoutDto());

        // THEN: NSEE
    }

    @Test
    public void getAvailableProfileByName_whenProfileFound_shouldReturnIt() {
        // GIVEN
        EditorLayoutDto layoutObject = new EditorLayoutDto();
        EditorLayoutDto.EditorProfileDto profileObject = new EditorLayoutDto.EditorProfileDto("profile name");
        layoutObject.getProfiles().add(profileObject);

        // WHEN
        EditorLayoutDto.EditorProfileDto actualProfile = EditorLayoutHelper.getAvailableProfileByName("profile name", layoutObject);

        // THEN
        assertThat(actualProfile).isEqualTo(profileObject);
    }
}