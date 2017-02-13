package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public long getTotalViewCount() {
        return views.values().stream()
                .mapToLong(Collection::size)
                .sum();
    }

    public Map<Integer, Short> getIndex() {
        return index;
    }

    public Map<Integer, List<CameraViewEnhanced>> getViews() {
        return views;
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
