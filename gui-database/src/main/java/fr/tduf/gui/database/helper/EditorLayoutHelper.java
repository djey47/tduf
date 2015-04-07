package fr.tduf.gui.database.helper;

import fr.tduf.gui.database.dto.EditorLayoutDto;

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
}