package fr.tduf.libunlimited.common.game.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.game.domain.bin.GameVersion;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static fr.tduf.libunlimited.common.game.FileConstants.HASH_GAME_BINARY_1_45A;
import static fr.tduf.libunlimited.common.game.FileConstants.HASH_GAME_BINARY_1_66A;
import static fr.tduf.libunlimited.common.game.domain.bin.GameVersion.*;

/**
 * Provides basic information about game process
 */
public class GameStatusHelper {
    private static final String THIS_CLASS_NAME = GameStatusHelper.class.getSimpleName();

    private GameStatusHelper() {}

    /**
     * @return TDU game version
     */
    public static GameVersion resolveGameVersion(String binaryPath) {
        if (binaryPath == null) {
            return NO_GAME_BINARY;
        }

        File binaryFile = new File(binaryPath);
        if (!binaryFile.exists()) {
            return NO_GAME_BINARY;
        }

        byte[] hash = hashGameBinary(binaryFile);

        Log.debug(THIS_CLASS_NAME, "Game binary hash: " + ArrayUtils.toString(hash));

        if (Arrays.equals(HASH_GAME_BINARY_1_45A, hash)) {
            return GENUINE_1_45A;
        }

        if (Arrays.equals(HASH_GAME_BINARY_1_66A, hash)) {
            return GENUINE_1_66A;
            // TODO resolve Magepack version
        }

        return UNKNOWN;
    }

    private static byte[] hashGameBinary(File binaryFile) {
        try {
            byte[] bytes = Files.readAllBytes(binaryFile.toPath());
            return MessageDigest.getInstance("SHA").digest(bytes);
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.error(THIS_CLASS_NAME, "Unable to compute hash for game binary file: " + binaryFile, e);
            return null;
        }
    }
}
