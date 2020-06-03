package fr.tduf.libunlimited.low.files.world.spots.rw;

import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;
import fr.tduf.libunlimited.low.files.world.spots.domain.MapSpotsInfo;

import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Reads contents of Hawai.spt file (map spots?)
 */
public class MapSpotsParser extends GenericParser<MapSpotsInfo> {
    private MapSpotsParser(XByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Loads data from a byte array stream.
     */
    public static MapSpotsParser load(XByteArrayInputStream inputStream) throws IOException {
        return new MapSpotsParser(
                requireNonNull(inputStream, "A stream containing spot contents is required"));
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/SPT-hawai-map.json";
    }

    @Override
    public FileStructureDto getStructure() {
        return null;
    }

    @Override
    protected MapSpotsInfo generate() {
        MapSpotsInfo mapSpotsInfo = new MapSpotsInfo();

        mapSpotsInfo.getMapSpots().addAll(getDataStore().getRepeatedValues("spots").stream()
                .map(subStore -> {
                    MapSpotsInfo.MapSpotInfo mapSpotInfo = new MapSpotsInfo.MapSpotInfo();

                    String spotIdentifier = subStore.getText("spotIdentifier").orElse("");
                    String spotBankFileName = subStore.getText("spotBankFileName").orElse("");
                    float spotXCoordinate = subStore.getFloatingPoint("spotCoordinatesX").orElse(0.0f);
                    float spotYCoordinate = subStore.getFloatingPoint("spotCoordinatesY").orElse(0.0f);
                    float spotZCoordinate = subStore.getFloatingPoint("spotCoordinatesZ").orElse(0.0f);
                    Long spotType = subStore.getInteger("spotType").orElse(0L);
                    Long spotSubType = subStore.getInteger("spotSubType").orElse(0L);
                    Long challengeDisplayCondition = subStore.getInteger("challengeOption").orElse(0L);
                    mapSpotInfo.setId(spotIdentifier);
                    mapSpotInfo.setBnkFileName(spotBankFileName);
                    mapSpotInfo.setXCoordinate(spotXCoordinate);
                    mapSpotInfo.setYCoordinate(spotYCoordinate);
                    mapSpotInfo.setZCoordinate(spotZCoordinate);
                    mapSpotInfo.setType(spotType.intValue());
                    mapSpotInfo.setSubType(spotSubType.intValue());
                    mapSpotInfo.setChallengeOption(challengeDisplayCondition.intValue());

                    return mapSpotInfo;
                })
                .collect(toList()));

        return mapSpotsInfo;
    }
}
