package fr.tduf.libunlimited.high.files.common.patcher.domain;

import java.util.Optional;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class PatchProperties extends Properties {

    /**
     * Stores value associated to a placeholder.
     */
    public void register(String placeholder, String value) {
        setProperty(
                requireNonNull(placeholder, "Placeholder is required."),
                requireNonNull(value, "Value is required")
        );
    }

    /**
     * @return value associated to requested placeholder if it exists, empty otherwise.
     */
    public Optional<String> retrieve(String placeholder) {
        requireNonNull(placeholder, "Placeholder is required.");

        return Optional.ofNullable(getProperty(placeholder))
                .map(String::trim);
    }

    /**
     * @return a deep copy of this property store.
     */
    public PatchProperties makeCopy() {
        PatchProperties patchProperties = new PatchProperties();

        cloneAllProperties(patchProperties);        
        
        return patchProperties;
    }

    protected void cloneAllProperties(PatchProperties patchProperties) {
        forEach((prop, v) -> {
            final String placeholder = (String) prop;
            final String value = (String) v;

            requireNonNull(patchProperties, "Patch Properties are required")
                    .register(placeholder, value);
        });
    }

    protected void registerIfNotExists(String placeholderName, String value) {
        if (retrieve(placeholderName).isPresent()) {
            return;
        }

        register(placeholderName, value);
    }
}
