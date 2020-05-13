package fr.tduf.libunlimited.low.files.savegame.rw;

import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;
import fr.tduf.libunlimited.low.files.savegame.domain.SaveGame;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Allow to parse from Commondt savegame files
 */
public class CommonDataParser extends GenericParser<SaveGame> {
    private CommonDataParser(XByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Loads data from a byte array stream.
     */
    public static CommonDataParser load(XByteArrayInputStream inputStream) throws IOException {
        return new CommonDataParser(
                requireNonNull(inputStream, "A stream containing common contents is required"));
    }

    @Override
    protected SaveGame generate() {
        return null;
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/PLAYERSAVE-COMMONDT-map.json";
    }
}
