package fr.tduf.libunlimited.low.files.savegame.helper;

import fr.tduf.libunlimited.low.files.savegame.domain.SaveGame;
import fr.tduf.libunlimited.low.files.savegame.rw.CommonDataParser;
import fr.tduf.libunlimited.low.files.savegame.rw.PlayerSaveParser;

import static java.util.Objects.requireNonNull;

public class SavegameHelper {
    /**
     * @return merged savegame information from both parsers
     */
    public static SaveGame buildSaveGameFromParsers(CommonDataParser commonDataParser, PlayerSaveParser playerSaveParser) {
        requireNonNull(commonDataParser, "Parser of common data is required.");
        requireNonNull(playerSaveParser, "Parser of player save is required.");

        return null;
    }
}
