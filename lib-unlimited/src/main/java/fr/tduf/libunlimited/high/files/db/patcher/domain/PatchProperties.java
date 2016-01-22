package fr.tduf.libunlimited.high.files.db.patcher.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Stores all information to resolve eventual placeholders in change objects
 */
public class PatchProperties {

    private final Map<String, String> infoStore = new HashMap<>();

    /**
     * Stores value associated to a placeholder.
     */
    public void store(String placeholder, String value) {
        infoStore.put(
                requireNonNull(placeholder, "Placeholder is required."),
                requireNonNull(value, "Value is required"));
    }

    /**
     * @return value associated tyo requested placeholder if it exists, empty otherwise.
     */
    public Optional<String> retrieve(String placeholder) {
        requireNonNull(placeholder, "Placeholder is required.");

        return Optional.ofNullable(infoStore.get(placeholder));
    }
}
