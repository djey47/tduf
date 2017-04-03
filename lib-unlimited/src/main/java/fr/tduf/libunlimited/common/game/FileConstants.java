package fr.tduf.libunlimited.common.game;

import java.util.regex.Pattern;

import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.EXTENSION_BANKS;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Constants for common game file names and directories
 */
public class FileConstants {
    public static final String DIRECTORY_EURO = "Euro";
    public static final String DIRECTORY_BANKS = "Bnk";
    public static final String DIRECTORY_AVATAR = "Avatar";
    public static final String DIRECTORY_DATABASE = "Database";
    public static final String DIRECTORY_FRONT_END = "FrontEnd";
    public static final String DIRECTORY_INTERIOR = "Interior";
    public static final String DIRECTORY_LEVEL = "Level";
    public static final String DIRECTORY_SOUNDS = "Sound";
    public static final String DIRECTORY_TUTORIAL = "Tutorial";
    public static final String DIRECTORY_VEHICLES = "Vehicules";
    public static final String DIRECTORY_HUDS = "Gauges";
    public static final String DIRECTORY_HAWAI = "Hawai";
    public static final String DIRECTORY_SPOTS = "Spots";
    public static final String DIRECTORY_CLOTHES = "CLOTHES";
    public static final String DIRECTORY_RIM = "Rim";
    public static final String DIRECTORY_MAP_SCREENS = "MapScreens";
    public static final String DIRECTORY_ALL_RES = "AllRes";
    public static final String DIRECTORY_HI_RES = "HiRes";
    public static final String DIRECTORY_LOW_RES = "LowRes";

    public static final String FORMAT_REGULAR_BANK = "%s." + EXTENSION_BANKS;
    public static final String FORMAT_VEHICLE_INT_BANK = "%s_I." + EXTENSION_BANKS;
    public static final String FORMAT_REGULAR_SOUND = "%s.wav";

    public static final Pattern PATTERN_INTERIOR_MODEL_BANK_FILE = Pattern.compile(".+_I\\.bnk", CASE_INSENSITIVE);
    public static final Pattern PATTERN_RIM_BANK_FILE = Pattern.compile(".+_([FR])_([0-9]{2})\\.bnk", CASE_INSENSITIVE);

    public static final String PREFIX_SPOT_EXTERIOR_BANK = "e";
    public static final String PREFIX_SPOT_INTERIOR_BANK = "i";
    public static final String PREFIX_SPOT_GARAGE_BANK = "g";
    public static final String PREFIX_SPOT_LOUNGE_BANK = "l";

    public static final String INDICATOR_FRONT_RIMS = "F";
    public static final String INDICATOR_REAR_RIMS = "R";

    public static final String SUFFIX_INTERIOR_BANK_FILE = "_I";
    public static final String SUFFIX_AUDIO_BANK_FILE = "_audio";

    private FileConstants() {}
}
