package fr.tduf.libunlimited.high.files.db.patcher.helper;

import static java.lang.String.format;

/**
 * Hosts all database patch placeholders
 */
public class PlaceholderConstants {
    private static final String FULL_PLACEHOLDER_FMT = "{%s}";

    private static final String PLACEHOLDER_NAME_FMT_EXTERIOR_SECONDARY_COLOR = "COLORID.S.%d";
    private static final String PLACEHOLDER_NAME_FMT_EXTERIOR_CALIPERS_COLOR = "CALLIPERSID.%d";
    private static final String PLACEHOLDER_NAME_FMT_EXTERIOR_NAME = "COLORNAME.%d";

    public static final String PLACEHOLDER_NAME_SLOT_REFERENCE = "SLOTREF";
    public static final String PLACEHOLDER_NAME_ID_CAR = "CARID";
    public static final String PLACEHOLDER_NAME_BANK = "BANKNAME";
    public static final String PLACEHOLDER_NAME_RESOURCE_BANK = "RES_BANKNAME";
    public static final String PLACEHOLDER_NAME_DEALER_REFERENCE = "DEALERREF";
    public static final String PLACEHOLDER_NAME_DEALER_SLOT = "DEALERSLOT";
    public static final String PLACEHOLDER_NAME_CAMERA = "CAMERA";

    public static final String PLACEHOLDER_NAME_FMT_RIMS_REFERENCE = "RIMREF.%d";
    public static final String PLACEHOLDER_NAME_FMT_RESOURCE_RIMS_BRAND = "RIMBRANDREF.%d";
    private static final String PLACEHOLDER_NAME_FMT_EXTERIOR_MAIN_COLOR = "COLORID.M.%d";
    public static final String PLACEHOLDER_NAME_FMT_INTERIOR_REFERENCE = "INTREF.%d";
    public static final String PLACEHOLDER_NAME_FMT_INTERIOR_ID_COLOR = "INTCOLORID.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_INTERIOR_MATERIAL = "INTMATERIALID.%d";
    public static final String PLACEHOLDER_NAME_FMT_RIMS_BANK = "BANKNAME.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK = "RES_BANKNAME.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_ID_COLOR = "COLORID.%s.%d";
    public static final String PLACEHOLDER_NAME_FMT_ID_CALIPERS_COLOR = "CALLIPERSID.%d";
    public static final String PLACEHOLDER_NAME_FMT_RESOURCE_COLOR = "RES_COLORNAME.%d";
    public static final String PLACEHOLDER_NAME_FMT_COLOR = "COLORNAME.%d";
    public static final String PLACEHOLDER_NAME_FMT_CUSTOM_CAM = "CAMERA.%s";

    public static final String SUFFIX_FRONT_RIMS = "FR";
    public static final String SUFFIX_REAR_RIMS = "RR";
    public static final String SUFFIX_MAIN_COLOR = "M";
    public static final String SUFFIX_SECONDARY_COLOR = "S";

    private PlaceholderConstants() {
    }

    public static String getPlaceHolderForExteriorMainColor(int exteriorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_EXTERIOR_MAIN_COLOR, exteriorIndex));
    }

    public static String getPlaceHolderForExteriorSecondaryColor(int exteriorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_EXTERIOR_SECONDARY_COLOR, exteriorIndex));
    }

    public static String getPlaceHolderForExteriorCalipersColor(int exteriorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_EXTERIOR_CALIPERS_COLOR, exteriorIndex));
    }

    public static String getPlaceHolderForExteriorName(int exteriorIndex) {
        return toFormattedPlaceHolder(format(PLACEHOLDER_NAME_FMT_EXTERIOR_NAME, exteriorIndex));
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

    private static String toFormattedPlaceHolder(String placeHolder) {
        return format(FULL_PLACEHOLDER_FMT, placeHolder);
    }
}
