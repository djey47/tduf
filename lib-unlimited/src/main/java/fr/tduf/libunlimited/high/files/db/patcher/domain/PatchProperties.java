package fr.tduf.libunlimited.high.files.db.patcher.domain;

import java.util.Optional;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

/**
 * Stores all information to resolve eventual placeholders in change objects
 */
public class PatchProperties extends Properties {

    /**
     * Stores value associated to a placeholder.
     */
    public void store(String placeholder, String value) {
        setProperty(
                requireNonNull(placeholder, "Placeholder is required."),
                requireNonNull(value, "Value is required")
        );
    }

    /**
     * @return value associated tyo requested placeholder if it exists, empty otherwise.
     */
    public Optional<String> retrieve(String placeholder) {
        requireNonNull(placeholder, "Placeholder is required.");

        return Optional.ofNullable(getProperty(placeholder));
    }

    /**
     * @return a deep copy of this property store.
     */
    public PatchProperties makeCopy() {
        final PatchProperties patchProperties = new PatchProperties();

        entrySet().forEach((property) -> {
            final String placeholder = (String) property.getKey();
            final String value = (String) property.getValue();

            patchProperties.store(placeholder, value);
        });

        return patchProperties;
    }
}
