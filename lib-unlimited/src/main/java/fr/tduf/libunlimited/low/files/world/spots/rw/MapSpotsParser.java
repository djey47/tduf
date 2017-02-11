package fr.tduf.libunlimited.low.files.world.spots.rw;

import fr.tduf.libunlimited.low.files.research.rw.GenericParser;
import fr.tduf.libunlimited.low.files.world.spots.domain.MapSpotsInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Reads contents of Hawai.spt file (map spots?)
 */
public class MapSpotsParser extends GenericParser<MapSpotsInfo> {
    private MapSpotsParser(ByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Loads data from a byte array stream.
     */
    public static MapSpotsParser load(ByteArrayInputStream inputStream) throws IOException {
        return new MapSpotsParser(
                requireNonNull(inputStream, "A stream containing spot contents is required"));
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/SPT-hawai-map.json";
    }

    @Override
    protected MapSpotsInfo generate() {
        MapSpotsInfo mapSpotsInfo = new MapSpotsInfo();

        mapSpotsInfo.getMapSpots().addAll(getDataStore().getRepeatedValues("spots").stream()
                .map(subStore -> {
                    MapSpotsInfo.MapSpotInfo mapSpotInfo = new MapSpotsInfo.MapSpotInfo();

                    String spotIdentifier = subStore.getText("spotIdentifier").orElse("");
                    String spotBankFileName = subStore.getText("spotBankFileName").orElse("");
                    mapSpotInfo.setId(spotIdentifier);
                    mapSpotInfo.setBnkFileName(spotBankFileName);

                    return mapSpotInfo;
                })
                .collect(toList()));

        return mapSpotsInfo;
    }
}
