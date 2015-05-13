package fr.tduf.gui.database.helper;

import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Provides static methods to handle layout objects.
 */
public class EditorLayoutHelper {

    /**
     * @return the available layout profile if it exists in provided layout object.
     */
    public static EditorLayoutDto.EditorProfileDto getAvailableProfileByName(String profileName, EditorLayoutDto layoutObject) {
        requireNonNull(layoutObject, "Editor layout object is required.");

        return layoutObject.getProfiles().stream()

                .filter((profile) -> profile.getName().equals(profileName))

                .findAny().get();
    }

    /**
     * @return field settings if they exist for provided field rank, absent otherwise.
     */
    public static Optional<FieldSettingsDto> getFieldSettingsByRank(int fieldRank, EditorLayoutDto.EditorProfileDto profileObject) {
        requireNonNull(profileObject, "Editor profile object is required.");

        return profileObject.getFieldSettings().stream()

                .filter((settings) -> settings.getRank() == fieldRank)

                .findAny();
    }

    /**
     * @return
     */
    public static Optional<FieldSettingsDto> getFieldSettingsByRankAndProfileName(int fieldRank, String profileName, EditorLayoutDto layoutObject) {
        requireNonNull(layoutObject, "Editor layout object is required.");

        EditorLayoutDto.EditorProfileDto currentProfile = getAvailableProfileByName(profileName, layoutObject);
        return getFieldSettingsByRank(fieldRank, currentProfile);
    }

    /**
     * @return
     */
    public static int getFieldPrioritySettingByRank(int fieldRank, String profileName, EditorLayoutDto layoutObject) {
        requireNonNull(layoutObject, "Editor layout object is required.");

        Optional<FieldSettingsDto> potentialFieldSettingsObject = getFieldSettingsByRankAndProfileName(fieldRank, profileName, layoutObject);
        int priority = 0;
        if (potentialFieldSettingsObject.isPresent()) {
            priority = potentialFieldSettingsObject.get().getPriority();
        }
        return priority;
    }
}