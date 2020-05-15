package fr.tduf.libunlimited.low.files.research.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.Entry.comparingByKey;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Stores and provides information about links within a file
 */
public class LinksContainer {
    private final Map<Integer, String> linkSources;
    private final Map<Integer, String> linkTargets;

    public LinksContainer() {
        linkSources = new HashMap<>();
        linkTargets = new HashMap<>();
    }

    /**
     * Adds link source with name and address
     * @param fieldKey  : full datastore key of this link
     * @param address   : address in file (in bytes)
     */
    public void registerSource(String fieldKey, int address) {
        register(fieldKey, address, linkSources);
    }

    /**
     * Adds link target with name and address
     * @param fieldKey  : full datastore key of this link
     * @param address   : address in file (in bytes)
     */
    public void registerTarget(String fieldKey, int address) {
        register(fieldKey, address, linkTargets);
    }

    /**
     * Deletes all sources and targets in this container
     */
    public void clear() {
        linkSources.clear();
        linkTargets.clear();
    }

    /**
     * @return true if at least one link source or link target are present (joined or not)
     */
    public boolean hasLinks() {
        return !linkSources.isEmpty() || !linkTargets.isEmpty();
    }

    /**
     * Checks that all links sources and targets can be joined properly
     */
    public void validate() {
        int sourceCount = linkSources.size();
        int targetCount = linkTargets.size();

        if (sourceCount != targetCount) {
            throw new IllegalStateException(String.format("Mismatch in links container: %d source(s) VS %d target(s)", sourceCount, targetCount));
        }

        // Each target must have its source
        long matchingCount = linkTargets.keySet().parallelStream()
                .filter(linkSources::containsKey)
                .count();

        long unlinkedCount = sourceCount - matchingCount;
        if (unlinkedCount != 0) {
            throw new IllegalStateException(String.format("Issue in links container: %d source(s) and target(s) linked, %d remaining", matchingCount, unlinkedCount));
        }
    }

    /**
     * @return all links sources, ordered by increasing address
     */
    public List<Map.Entry<Integer, String>> getSourcesSortedByAddress() {
        return linkSources.entrySet().parallelStream()
                .sorted(comparingByKey())
                .collect(toList());
    }

    /**
     * @return existing field key at provided address, otherwise empty
     */
    public Optional<String> getSourceFieldKeyWithAddress(int address) {
        return getFieldKeyWithAddress(address, linkSources);
    }

    /**
     * @return existing field key at provided address, otherwise empty
     */
    public Optional<String> getTargetFieldKeyWithAddress(int address) {
        return getFieldKeyWithAddress(address, linkTargets);
    }

    /**
     * Visible for testing
     */
    void register(String fieldKey, int address, Map<Integer, String> register) {
        requireNonNull(fieldKey, "A complete field key must be provided");
        requireNonNull(fieldKey, "A register must be provided");

        if (address == 0) {
            return;
        }

        if (register.containsKey(address)) {
            String registerType = (register == linkSources) ? "source" : "target";
            throw new IllegalArgumentException(String.format("A field key has already been registered as %s @0x%08X (%d)", registerType, address, address));
        }

        register.put(address, fieldKey);
    }

    /**
     * Visible for testing
     */
    Optional<String> getFieldKeyWithAddress(int address, Map<Integer, String> register) {
        requireNonNull(register, "A register must be provided");

        return ofNullable(register.get(address));
    }

    /**
     * Visible for testing
     */
    Map<Integer, String> getSources() {
        return linkSources;
    }

    /**
     * Visible for testing
     */
    Map<Integer, String> getTargets() {
        return linkTargets;
    }
}
