package fr.tduf.libunlimited.high.files.db.patcher.helper;

import static java.lang.String.format;

/**
 * Hosts all database patch placeholders
 */
public class PlaceholderConstants {
    private static final String FULL_PLACEHOLDER_FMT = "{%s}";

    public static final String PLACEHOLDER_NAME_SLOT_REFERENCE = "SLOTREF";
    public static final String PLACEHOLDER_NAME_ID_CAR = "CARID";
    public static final String PLACEHOLDER_NAME_BANK = "BANKNAME";
    public static final String PLACEHOLDER_NAME_RESOURCE_BANK = "RES_BANKNAME";
    public static final String PLACEHOLDER_NAME_DEALER_REFERENCE = "DEALERREF";
    public static final String PLACEHOLDER_NAME_DEALER_SLOT = "DEALERSLOT";
    public static final String PLACEHOLDER_NAME_CAMERA = "CAMERA";

    public static final String PLACEHOLDER_NAME_FMT_RIMS_REFERENCE = "RIMREF.%d";
    public static final String PLACEHOLDER_NAME_FMT_RIM_BRAND_REFERENCE = "RIMBRANDREF.%d";
    public static final String PLACEHOLDER_NAME_FMT_RIM_WIDTH = "RIMWIDTH.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_RIM_HEIGHT = "RIMHEIGHT.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_RIM_DIAMETER = "RIMDIAM.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_RIM_NAME = "RIMNAME.%d";
    public static final String PLACEHOLDER_NAME_FMT_RESOURCE_RIMS_BRAND = "RIMBRANDREF.%d";
    public static final String PLACEHOLDER_NAME_FMT_RESOURCE_RIM_NAME = "RES_RIMNAME.%d";
    public static final String PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK = "RES_BANKNAME.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_RIMS_BANK = "BANKNAME.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_INTERIOR_REFERENCE = "INTREF.%d";
    public static final String PLACEHOLDER_NAME_FMT_INTERIOR_ID_COLOR = "INTCOLORID.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_INTERIOR_MATERIAL = "INTMATERIALID.%d";
    public static final String PLACEHOLDER_NAME_FMT_ID_COLOR = "COLORID.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_ID_INT_COLOR = "INTCOLORID.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_ID_CALIPERS_COLOR = "CALLIPERSID.%d";
    public static final String PLACEHOLDER_NAME_FMT_RESOURCE_COLOR = "RES_COLORNAME.%d";
    public static final String PLACEHOLDER_NAME_FMT_COLOR = "COLORNAME.%d";
    public static final String PLACEHOLDER_NAME_FMT_CUSTOM_CAM = "CAMERA.%s";

    public static final String SUFFIX_FRONT_RIMS = "FR";
    public static final String SUFFIX_REAR_RIMS = "RR";
    public static final String SUFFIX_MAIN_COLOR = "M";
    public static final String SUFFIX_SECONDARY_COLOR = "S";

    private PlaceholderConstants() {}

    public static String getPlaceHolderForExteriorMainColor(int exteriorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_ID_COLOR, SUFFIX_MAIN_COLOR, exteriorIndex));
    }

    public static String getPlaceHolderForExteriorSecondaryColor(int exteriorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_ID_COLOR, SUFFIX_SECONDARY_COLOR, exteriorIndex));
    }

    public static String getPlaceHolderForExteriorCalipersColor(int exteriorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_ID_CALIPERS_COLOR, exteriorIndex));
    }

    public static String getPlaceHolderForExteriorNameResource(int exteriorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RESOURCE_COLOR, exteriorIndex));
    }

    public static String getPlaceHolderForInteriorMainColor(int interiorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_INTERIOR_ID_COLOR, SUFFIX_MAIN_COLOR, interiorIndex));
    }

    public static String getPlaceHolderForInteriorSecondaryColor(int interiorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_INTERIOR_ID_COLOR, SUFFIX_SECONDARY_COLOR, interiorIndex));
    }

    public static String getPlaceHolderForInteriorMaterial(int interiorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_INTERIOR_MATERIAL, interiorIndex));
    }

    public static String getPlaceHolderForRimBrand(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIM_BRAND_REFERENCE, rimIndex));
    }

    public static String getPlaceHolderForFrontRimFileNameResource(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK, SUFFIX_FRONT_RIMS, rimIndex));
    }

    public static String getPlaceHolderForRearRimFileNameResource(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK, SUFFIX_REAR_RIMS, rimIndex));
    }

    public static String getPlaceHolderForFrontRimFileName(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIMS_BANK, SUFFIX_FRONT_RIMS, rimIndex));
    }

    public static String getPlaceHolderForRearRimFileName(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIMS_BANK, SUFFIX_REAR_RIMS, rimIndex));
    }

    public static String getPlaceHolderForRimNameResource(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RESOURCE_RIM_NAME, rimIndex));

    }

    public static String getPlaceHolderForRimName(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIM_NAME, rimIndex));

    }

    public static String getPlaceHolderForFrontRimWidth(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIM_WIDTH, SUFFIX_FRONT_RIMS, rimIndex));
    }

    public static String getPlaceHolderForRearRimWidth(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIM_WIDTH, SUFFIX_REAR_RIMS, rimIndex));
    }

    public static String getPlaceHolderForFrontRimHeight(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIM_HEIGHT, SUFFIX_FRONT_RIMS, rimIndex));
    }

    public static String getPlaceHolderForRearRimHeight(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIM_HEIGHT, SUFFIX_REAR_RIMS, rimIndex));
    }

    public static String getPlaceHolderForFrontRimDiameter(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIM_DIAMETER, SUFFIX_FRONT_RIMS, rimIndex));
    }

    public static String getPlaceHolderForRearRimDiameter(int rimIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_RIM_DIAMETER, SUFFIX_REAR_RIMS, rimIndex));
    }

    /**
     * @return true if provided placeholder matches one used for ID_CAR
     */
    static boolean isPlaceholderForCarIdentifier(String placeholderName) {
        return PLACEHOLDER_NAME_ID_CAR.equals(placeholderName);
    }

    private static String toFormattedPlaceHolder(String placeHolder) {
        return format(FULL_PLACEHOLDER_FMT, placeHolder);
    }
}
