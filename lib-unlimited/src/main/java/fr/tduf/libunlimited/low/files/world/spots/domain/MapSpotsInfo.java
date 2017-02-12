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
        private float xCoordinate;
        private float yCoordinate;
        private float zCoordinate;
        private SpotCategory type;
        private int subType;
        private int challengeOption;

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

        public void setXCoordinate(float xCoordinate) {
            this.xCoordinate = xCoordinate;
        }

        public float getXCoordinate() {
            return xCoordinate;
        }

        public void setYCoordinate(float yCoordinate) {
            this.yCoordinate = yCoordinate;
        }

        public float getYCoordinate() {
            return yCoordinate;
        }

        public void setZCoordinate(float zCoordinate) {
            this.zCoordinate = zCoordinate;
        }

        public float getZCoordinate() {
            return zCoordinate;
        }

        public void setType(int type) {
            this.type = SpotCategory.fromCategoryId(type);
        }

        public SpotCategory getType() {
            return type;
        }

        public void setSubType(int subType) {
            this.subType = subType;
        }

        public int getSubType() {
            return subType;
        }

        public void setChallengeOption(int displayCondition) {
            this.challengeOption = displayCondition;
        }

        public int getChallengeOption() {
            return challengeOption;
        }
    }
}
