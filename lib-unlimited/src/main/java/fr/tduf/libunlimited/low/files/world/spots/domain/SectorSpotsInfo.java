package fr.tduf.libunlimited.low.files.world.spots.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * All parsed information about driving map spots (dealers, races, ...)
 */
public class SectorSpotsInfo {
    private final List<SectorSpotInfo> sectorSpots = new ArrayList<>();

    public List<SectorSpotInfo> getSectorSpots() {
        return sectorSpots;
    }

    /**
     * Data for single map spot
     */
    public static class SectorSpotInfo {
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
