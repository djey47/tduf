package fr.tduf.gui.database.helper;

import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Provides static methods to handle layout objects.
 */
public class EditorLayoutHelper {

    /**
     * @return the available layout profile if it exists in provided layout object.
     */
    public static EditorLayoutDto.EditorProfileDto getAvailableProfileByName(String profileName, EditorLayoutDto layoutObject) throws IllegalArgumentException {
        requireNonNull(layoutObject, "Editor layout object is required.");

        Optional<EditorLayoutDto.EditorProfileDto> potentialProfileObject = layoutObject.getProfiles().stream()

                .filter((profile) -> profile.getName().equals(profileName))

                .findAny();

        if (!potentialProfileObject.isPresent()) {
            throw new IllegalArgumentException("Unknown profile name: " + profileName);
        }

        return potentialProfileObject.get();
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
     * @return field settings if they exist for provided field rank and profile name, absent otherwise.
     */
    public static Optional<FieldSettingsDto> getFieldSettingsByRankAndProfileName(int fieldRank, String profileName, EditorLayoutDto layoutObject) throws IllegalArgumentException {
        requireNonNull(layoutObject, "Editor layout object is required.");

        EditorLayoutDto.EditorProfileDto currentProfile = getAvailableProfileByName(profileName, layoutObject);
        return getFieldSettingsByRank(fieldRank, currentProfile);
    }

    /**
     * @return field priority setting if it exists, 0 otherwise.
     */
    public static int getFieldPrioritySettingByRank(int fieldRank, String profileName, EditorLayoutDto layoutObject) throws IllegalArgumentException {
        requireNonNull(layoutObject, "Editor layout object is required.");

        Optional<FieldSettingsDto> potentialFieldSettingsObject = getFieldSettingsByRankAndProfileName(fieldRank, profileName, layoutObject);
        int priority = 0;
        if (potentialFieldSettingsObject.isPresent()) {
            priority = potentialFieldSettingsObject.get().getPriority();
        }
        return priority;
    }

    /**
     * @return entry label field ranks setting.
     */
    public static List<Integer> getEntryLabelFieldRanksSettingByProfile(String profileName, EditorLayoutDto layoutObject) throws IllegalArgumentException {
        requireNonNull(profileName, "Profile name is required.");
        requireNonNull(layoutObject, "Editor layout object is required.");

        return getAvailableProfileByName(profileName, layoutObject).getEntryLabelFieldRanks();
    }
}