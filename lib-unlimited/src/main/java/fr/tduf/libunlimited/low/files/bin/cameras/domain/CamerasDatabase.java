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
public class CamerasDatabase {
    @JsonIgnore
    private DataStore originalDataStore;

    private Map<Integer, Short> index;
    private Map<Integer, List<CameraView>> views;

    private CamerasDatabase() {}

    public static CamerasDatabaseBuilder builder() {
        return new CamerasDatabaseBuilder();
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
    public Stream<Map.Entry<Integer, List<CameraView>>> getViewEntriesAsStream() {
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
    public List<CameraView> getViewsForCameraSet(int setIdentifier) {
        if (!views.containsKey(setIdentifier)) {
            throw new NoSuchElementException("No camera set with id=" + setIdentifier);
        }
        return views.get(setIdentifier);
    }

    /**
     * @return all available views, indexed by kind, for specified set identifier
     * @throws NoSuchElementException if such a set does not exist
     */
    public Map<ViewKind, CameraView> getViewsByKindForCameraSet(int setIdentifier) {
        return getViewsForCameraSet(setIdentifier).stream()
                .collect(toMap(CameraView::getKind, v -> v));
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
    public void updateViews(int setIdentifier, List<CameraView> newViews) {
        views.put(setIdentifier, newViews);
    }

    /**
     * Removes all attached views from index and settings
     */
    public void removeSet(int cameraSetIdentifier) {
        index.remove(cameraSetIdentifier);
        views.remove(cameraSetIdentifier);
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

    public static class CamerasDatabaseBuilder {
        private DataStore originalDataStore;

        private final Map<Integer, Short> index = new LinkedHashMap<>(0);
        private final Map<Integer, List<CameraView>> views = new LinkedHashMap<>(0);

        public CamerasDatabaseBuilder fromDatastore(DataStore originalDataStore) {
            this.originalDataStore = originalDataStore;
            return this;
        }

        public CamerasDatabaseBuilder withIndex(Map<Integer, Short> index) {
            this.index.putAll(index);
            return this;
        }

        public CamerasDatabaseBuilder withViews(Map<Integer, List<CameraView>> views) {
            this.views.putAll(views);
            return this;
        }

        public CamerasDatabase build() {
            CamerasDatabase camerasDatabase = new CamerasDatabase();

            camerasDatabase.originalDataStore = originalDataStore;
            camerasDatabase.index = requireNonNull(index, "Index is required");
            camerasDatabase.views = requireNonNull(views, "Views are required");

            return camerasDatabase;
        }
    }
}
