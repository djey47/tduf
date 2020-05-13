package fr.tduf.libunlimited.low.files.savegame.rw;

import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;
import fr.tduf.libunlimited.low.files.savegame.domain.SaveGame;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Allow to parse from Playersave savegame files
 */
public class PlayerSaveParser extends GenericParser<SaveGame> {
    private PlayerSaveParser(XByteArrayInputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Loads data from a byte array stream.
     */
    public static PlayerSaveParser load(XByteArrayInputStream inputStream) throws IOException {
        return new PlayerSaveParser(
                requireNonNull(inputStream, "A stream containing save game contents is required"));
    }

    @Override
    protected SaveGame generate() {
        return null;
    }

    @Override
    public String getStructureResource() {
        return "/files/structures/PLAYERSAVE-map.json";
    }
}
