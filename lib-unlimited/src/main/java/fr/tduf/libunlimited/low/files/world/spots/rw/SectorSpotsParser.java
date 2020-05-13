package fr.tduf.libunlimited.low.files.world.spots.rw;

import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;
import fr.tduf.libunlimited.low.files.world.spots.domain.SectorSpotsInfo;

import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Reads contents of Sector_xxxx.spt file (driving spots?)
 */
public class SectorSpotsParser extends GenericParser<SectorSpotsInfo> {
    private SectorSpotsParser(XByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Loads data from a byte array stream.
     */
    public static SectorSpotsParser load(XByteArrayInputStream inputStream) throws IOException {
        return new SectorSpotsParser(
                requireNonNull(inputStream, "A stream containing spot contents is required"));
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/SPT-sector-map.json";
    }

    @Override
    protected SectorSpotsInfo generate() {
        SectorSpotsInfo mapSpotsInfo = new SectorSpotsInfo();

        mapSpotsInfo.getSectorSpots().addAll(getDataStore().getRepeatedValues("spots").stream()
                .map(subStore -> {
                    SectorSpotsInfo.SectorSpotInfo mapSpotInfo = new SectorSpotsInfo.SectorSpotInfo();

                    String spotIdentifier = subStore.getText("spotIdentifier").orElse("");
                    String spotBankFileName = subStore.getText("spotBankFileName").orElse("");
                    float spotCoordinatesX = subStore.getFloatingPoint("spotCoordinatesX").orElse(0.0f);
                    float spotCoordinatesZ = subStore.getFloatingPoint("spotCoordinatesZ").orElse(0.0f);
                    float spotDimension1 = subStore.getFloatingPoint("spotDimension1").orElse(0.0f);
                    float spotDimension2 = subStore.getFloatingPoint("spotDimension2").orElse(0.0f);

                    mapSpotInfo.setId(spotIdentifier);
                    mapSpotInfo.setBnkFileName(spotBankFileName);
                    mapSpotInfo.setXCoordinate(spotCoordinatesX);
                    mapSpotInfo.setZCoordinate(spotCoordinatesZ);
                    mapSpotInfo.setDimension1(spotDimension1);
                    mapSpotInfo.setDimension2(spotDimension2);

                    return mapSpotInfo;
                })
                .collect(toList()));

        return mapSpotsInfo;
    }
}
