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
        private float xCoordinate;
        private float zCoordinate;
        private float dimension1;
        private float dimension2;

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

        public void setXCoordinate(float XCoordinate) {
            this.xCoordinate = XCoordinate;
        }

        public float getXCoordinate() {
            return xCoordinate;
        }

        public void setZCoordinate(float ZCoordinate) {
            this.zCoordinate = ZCoordinate;
        }

        public float getZCoordinate() {
            return zCoordinate;
        }

        public void setDimension1(float dimension1) {
            this.dimension1 = dimension1;
        }

        public float getDimension1() {
            return dimension1;
        }

        public void setDimension2(float dimension2) {
            this.dimension2 = dimension2;
        }

        public float getDimension2() {
            return dimension2;
        }
    }
}
