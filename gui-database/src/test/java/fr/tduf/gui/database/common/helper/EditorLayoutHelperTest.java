package fr.tduf.gui.database.common.helper;


import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EditorLayoutHelperTest {
    @Test
    void getAvailableProfileByName_whenLayoutObjectNull_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> EditorLayoutHelper.getAvailableProfileByName(null, null));
    }

    @Test
    void getAvailableProfileByName_whenProfileNotFound_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> EditorLayoutHelper.getAvailableProfileByName("", new EditorLayoutDto()));
    }

    @Test
    void getAvailableProfileByName_whenProfileFound_shouldReturnIt() {
        // GIVEN
        EditorLayoutDto.EditorProfileDto profileObject = createProfileObject();
        EditorLayoutDto layoutObject = createLayoutWithOneProfile(profileObject);

        // WHEN
        EditorLayoutDto.EditorProfileDto actualProfile = EditorLayoutHelper.getAvailableProfileByName("profile name", layoutObject);

        // THEN
        assertThat(actualProfile).isEqualTo(profileObject);
    }

    @Test
    void getDefaultProfile_shouldReturnIt() {
        // GIVEN
        EditorLayoutDto.EditorProfileDto profileObject = createDefaultProfileObject();
        EditorLayoutDto layoutObject = createLayoutWithOneProfile(profileObject);

        // WHEN
        EditorLayoutDto.EditorProfileDto actualProfile = EditorLayoutHelper.getDefaultProfile(layoutObject);

        // THEN
        assertThat(actualProfile).isEqualTo(profileObject);
    }

    @Test
    void getDefaultProfile_whenNoDefaultProfile_shouldThrowException() {
        // GIVEN
        EditorLayoutDto.EditorProfileDto profileObject = createProfileObject();
        EditorLayoutDto layoutObject = createLayoutWithOneProfile(profileObject);

        // WHEN-THEN
        assertThrows(IllegalArgumentException.class,
                () -> EditorLayoutHelper.getDefaultProfile(layoutObject));
    }

    @Test
    void getFieldSettingsByRank_whenLayoutObjectNull_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> EditorLayoutHelper.getFieldSettingsByRank(0, null));
    }

    @Test
    void getFieldSettingsByRank_whenSettingsNotFound_shouldReturnAbsent() {
        // GIVEN-WHEN-THEN
        assertThat(EditorLayoutHelper.getFieldSettingsByRank(0, new EditorLayoutDto.EditorProfileDto())).isEmpty();
    }

    @Test
    void getFieldSettingsByRank_whenSettingsFound_shouldReturnThem() {
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

    @Test
    void getEntryLabelFieldRanksSetting_whenLayoutObjectNull_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile("", null));
    }

    @Test
    void getEntryLabelFieldRanksSetting_whenProfileNameNull_shouldThrowException() {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> EditorLayoutHelper.getEntryLabelFieldRanksSettingByProfile(null, new EditorLayoutDto()));
    }

    @Test
    void getEntryLabelFieldRanksSetting_whenSettingFound_shouldReturnIt() {
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

    private static EditorLayoutDto.EditorProfileDto createDefaultProfileObject() {
        return new EditorLayoutDto.EditorProfileDto("Vehicle slots");
    }

    private static EditorLayoutDto createLayoutWithOneProfile(EditorLayoutDto.EditorProfileDto profileObject) {
        EditorLayoutDto layoutObject = new EditorLayoutDto();
        layoutObject.getProfiles().add(profileObject);
        return layoutObject;
    }
}