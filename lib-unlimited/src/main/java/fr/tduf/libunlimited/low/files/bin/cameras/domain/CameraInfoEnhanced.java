package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Parsed cameras database contents
 */
public class CameraInfoEnhanced {
    private DataStore originalDataStore;

    private Map<Integer, Short> index;
    private Map<Integer, List<CameraViewEnhanced>> views;

    private CameraInfoEnhanced() {}

    public static CameraInfoEnhancedBuilder builder() {
        return new CameraInfoEnhancedBuilder();
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
     * @throws NoSuchElementException if such a set does not exists
     */
    public List<CameraViewEnhanced> getViewsForCameraSet(int setIdentifier) {
        if (!views.containsKey(setIdentifier)) {
            throw new NoSuchElementException("No camera set with id=" + setIdentifier);
        }
        return views.get(setIdentifier);
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
