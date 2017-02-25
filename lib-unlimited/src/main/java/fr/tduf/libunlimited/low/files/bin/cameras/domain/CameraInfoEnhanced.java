package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * Parsed cameras database contents
 */
// TODO add use sets info (source and target)
public class CameraInfoEnhanced {
    @JsonIgnore
    private DataStore originalDataStore;

    private Map<Integer, Short> index;
    private Map<Integer, List<CameraViewEnhanced>> views;

    private CameraInfoEnhanced() {}

    public static CameraInfoEnhancedBuilder builder() {
        return new CameraInfoEnhancedBuilder();
    }

    /**
     * @return all index entries, as stream
     */
    public Stream<Map.Entry<Integer, Short>> getIndexEntriesAsStream() {
        return index.entrySet().stream();
    }

    /**
     * @return all view entries, as stream
     */
    public Stream<Map.Entry<Integer, List<CameraViewEnhanced>>> getViewEntriesAsStream() {
        return views.entrySet().stream();
    }

    /**
     * @return views count, hosted by all camera sets
     */
    public long getTotalViewCount() {
        return views.values().stream()
                .mapToLong(Collection::size)
                .sum();
    }

    /**
     * @return true if a set with provided id exists in index or in settings
     */
    public boolean cameraSetExists(int cameraId) {
        return cameraSetExistsInIndex(cameraId) || cameraSetExistsInSettings(cameraId);
    }

    /**
     * @return true if a set with provided id exists in index
     */
    public boolean cameraSetExistsInIndex(int cameraId) {
        return index.containsKey(cameraId);
    }

    /**
     * @return true if a set with provided id exists in settings
     */
    public boolean cameraSetExistsInSettings(int cameraId) {
        return views.containsKey(cameraId);
    }

    /**
     * @return all views for specified set identifier
     * @throws NoSuchElementException if such a set does not exist
     */
    public List<CameraViewEnhanced> getViewsForCameraSet(int setIdentifier) {
        if (!views.containsKey(setIdentifier)) {
            throw new NoSuchElementException("No camera set with id=" + setIdentifier);
        }
        return views.get(setIdentifier);
    }

    /**
     * @return all available views, indexed by kind, for specified set identifier
     * @throws NoSuchElementException if such a set does not exist
     */
    public Map<ViewKind, CameraViewEnhanced> getViewsByKindForCameraSet(int setIdentifier) {
        return getViewsForCameraSet(setIdentifier).stream()
                .collect(toMap(CameraViewEnhanced::getKind, v -> v));
    }

    /**
     * Creates or replaces index entry for specified set identifier
     */
    public void updateIndex(int setIdentifier, short viewCount) {
        index.put(setIdentifier, viewCount);
    }

    /**
     * Creates or replaces index entry for specified set identifier
     */
    public void updateViews(int setIdentifier, List<CameraViewEnhanced> newViews) {
        views.put(setIdentifier, newViews);
    }

    public List<Integer> getAllSetIdentifiers() {
        return new ArrayList<>(views.keySet());
    }

    public int getIndexSize() {
        return index.size();
    }

    public int getSetsCount() {
        return views.size();
    }

    public DataStore getOriginalDataStore() {
        return originalDataStore;
    }

    public static class CameraInfoEnhancedBuilder {
        private DataStore originalDataStore;

        private final Map<Integer, Short> index = new LinkedHashMap<>(0);
        private final Map<Integer, List<CameraViewEnhanced>> views = new LinkedHashMap<>(0);

        public CameraInfoEnhancedBuilder fromDatastore(DataStore originalDataStore) {
            this.originalDataStore = originalDataStore;
            return this;
        }

        public CameraInfoEnhancedBuilder withIndex(Map<Integer, Short> index) {
            this.index.putAll(index);
            return this;
        }

        public CameraInfoEnhancedBuilder withViews(Map<Integer, List<CameraViewEnhanced>> views) {
            this.views.putAll(views);
            return this;
        }

        public CameraInfoEnhanced build() {
            CameraInfoEnhanced cameraInfoEnhanced = new CameraInfoEnhanced();

            cameraInfoEnhanced.originalDataStore = originalDataStore;
            cameraInfoEnhanced.index = requireNonNull(index, "Index is required");
            cameraInfoEnhanced.views = requireNonNull(views, "Views are required");

            return cameraInfoEnhanced;
        }
    }
}
