package fr.tduf.libunlimited.low.files.world.spots.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * All parsed information about Hawai map spots (dealers, races, ...)
 */
public class MapSpotsInfo {
    private final List<MapSpotInfo> mapSpots = new ArrayList<>();

    public List<MapSpotInfo> getMapSpots() {
        return mapSpots;
    }

    /**
     * Data for single map spot
     */
    public static class MapSpotInfo {
        private String id;
        private String bnkFileName;

        public String getBnkFileName() {
            return bnkFileName;
        }

        public void setBnkFileName(String bnkFileName) {
            this.bnkFileName = bnkFileName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
