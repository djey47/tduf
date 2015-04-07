package fr.tduf.gui.database.helper;

import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;

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
     * @return field settings if they exist for provided field name, absent otherwise.
     */
    public static Optional<FieldSettingsDto> getFieldSettingsByName(String fieldName, EditorLayoutDto.EditorProfileDto profileObject) {
        requireNonNull(profileObject, "Editor profile object is required.");

        return profileObject.getFieldSettings().stream()

                .filter((settings) -> settings.getName().equals(fieldName))

                .findAny();
    }
}