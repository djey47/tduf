package fr.tduf.gui.database.helper;


import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class EditorLayoutHelperTest {

    @Test(expected = NullPointerException.class)
    public void getAvailableProfileByName_whenLayoutObjectNull_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        EditorLayoutHelper.getAvailableProfileByName(null, null);

        // THEN: NPE
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAvailableProfileByName_whenProfileNotFound_shouldThrowException() {
        // GIVEN-WHEN
        EditorLayoutHelper.getAvailableProfileByName("", new EditorLayoutDto());

        // THEN: NSEE
    }

    @Test
    public void getAvailableProfileByName_whenProfileFound_shouldReturnIt() {
        // GIVEN
        EditorLayoutDto.EditorProfileDto profileObject = createProfileObject();
        EditorLayoutDto layoutObject = createLayoutWithOneProfile(profileObject);

        // WHEN
        EditorLayoutDto.EditorProfileDto actualProfile = EditorLayoutHelper.getAvailableProfileByName("profile name", layoutObject);

        // THEN
        assertThat(actualProfile).isEqualTo(profileObject);
    }

    @Test(expected = NullPointerException.class)
    public void getFieldSettingsByRank_whenLayoutObjectNull_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        EditorLayoutHelper.getFieldSettingsByRank(0, null);

        // THEN: NPE
    }

    @Test
    public void getFieldSettingsByRank_whenSettingsNotFound_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(EditorLayoutHelper.getFieldSettingsByRank(0, new EditorLayoutDto.EditorProfileDto())).isEmpty();
    }

    @Test
    public void getFieldSettingsByRank_whenSettingsFound_shouldReturnThem() {
        // GIVEN
        EditorLayoutDto.EditorProfileDto profileObject = createProfileObject();
        FieldSettingsDto fieldSettingsObject = new FieldSettingsDto(1);
        profileObject.getFieldSettings().add(fieldSettingsObject);

        // WHEN
        Optional<FieldSettingsDto> actualSettings = EditorLayoutHelper.getFieldSettingsByRank(1, profileObject);

        // THEN
        assertThat(actualSettings)
                .isPresent()
                .contains(fieldSettingsObject);
    }

    @Test(expected = NullPointerException.class)
    public void getEntryLabelFieldRanksSetting_whenLayoutObjectNull_shouldThrowException() {
        // GIVEN-WHEN
        EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile("", null);

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void getEntryLabelFieldRanksSetting_whenProfileNameNull_shouldThrowException() {
        // GIVEN-WHEN
        EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(null, new EditorLayoutDto());

        // THEN: NPE
    }

    @Test
    public void getEntryLabelFieldRanksSetting_whenSettingFound_shouldReturnIt() {
        // GIVEN
        EditorLayoutDto.EditorProfileDto profileObject = createProfileObject();
        profileObject.getEntryLabelFieldRanks().add(1);
        EditorLayoutDto layoutObject = createLayoutWithOneProfile(profileObject);

        // WHEN
        List<Integer> actualSetting = EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile("profile name", layoutObject);

        // THEN
        assertThat(actualSetting).containsExactly(1);
    }

    private static EditorLayoutDto.EditorProfileDto createProfileObject() {
        return new EditorLayoutDto.EditorProfileDto("profile name");
    }

    private static EditorLayoutDto createLayoutWithOneProfile(EditorLayoutDto.EditorProfileDto profileObject) {
        EditorLayoutDto layoutObject = new EditorLayoutDto();
        layoutObject.getProfiles().add(profileObject);
        return layoutObject;
    }
}