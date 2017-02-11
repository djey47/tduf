package fr.tduf.libunlimited.low.files.world.spots.rw;

import fr.tduf.libunlimited.low.files.research.rw.GenericParser;
import fr.tduf.libunlimited.low.files.world.spots.domain.SectorSpotsInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Reads contents of Sector_xxxx.spt file (driving spots?)
 */
public class SectorSpotsParser extends GenericParser<SectorSpotsInfo> {
    private SectorSpotsParser(ByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Loads data from a byte array stream.
     */
    public static SectorSpotsParser load(ByteArrayInputStream inputStream) throws IOException {
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
                    mapSpotInfo.setId(spotIdentifier);
                    mapSpotInfo.setBnkFileName(spotBankFileName);

                    return mapSpotInfo;
                })
                .collect(toList()));

        return mapSpotsInfo;
    }
}
