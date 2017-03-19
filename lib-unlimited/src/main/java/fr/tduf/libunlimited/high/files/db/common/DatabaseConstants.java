package fr.tduf.libunlimited.high.files.db.common;

/**
 * Gives all constants to access particular information in TDU database.
 */
public class DatabaseConstants {
    // GENERIC
    public static final String RESOURCE_VALUE_DEFAULT = "Default";
    public static final String RESOURCE_REF_DEFAULT = "";
    public static final String RESOURCE_VALUE_NONE = "??";

    // CAR PHYSICS, CAR RIMS, CAR COLORS
    public static final int FIELD_RANK_CAR_REF = 1;

    // BRANDS
    public static final int FIELD_RANK_BRAND_REF = 1;
    public static final int FIELD_RANK_MANUFACTURER_ID = 2;
    public static final int FIELD_RANK_MANUFACTURER_NAME = 3;
    public static final String REF_DEFAULT_BRAND = "62938337";

    // CAR COLORS
    public static final int FIELD_RANK_COLOR_NAME = 3;
    public static final int FIELD_RANK_INTERIOR_1 = 8;
    public static final int FIELD_RANK_INTERIOR_15 = 22;
    public static final int COUNT_INTERIORS = 15;
    public static final String CODE_EXTERIOR_COLOR_NONE = "53356127";
    public static final String CODE_EXTERIOR_COLOR_BLUE_01 = "54356127";
    public static final String RESOURCE_REF_UNKNOWN_COLOR_NAME = "53366457";

    // CLOTHES
    public static final int FIELD_RANK_FURNITURE_FILE = 2;

    // CAR PACKS
    public static final int FIELD_RANK_CAR_FILE_NAME_SWAP = 3;

    // CAR PHYSICS
    public static final int FIELD_RANK_CAR_BRAND = 2;
    public static final int FIELD_RANK_GROUP = 5;
    public static final int FIELD_RANK_CAR_FILE_NAME = 9;
    public static final int FIELD_RANK_DEFAULT_RIMS = 10;
    public static final int FIELD_RANK_HUD_FILE_NAME = 11;
    public static final int FIELD_RANK_CAR_REAL_NAME = 12;
    public static final int FIELD_RANK_CAR_MODEL_NAME = 13;
    public static final int FIELD_RANK_CAR_VERSION_NAME = 14;
    public static final int FIELD_RANK_ID_CAM = 98;
    public static final int FIELD_RANK_SECU1 = 100;
    public static final int FIELD_RANK_SECU2 = 101;
    public static final int FIELD_RANK_ID_CAR = 102;
    public static final String RESOURCE_REF_UNKNOWN_VEHICLE_NAME = "53338427";
    public static final String RESOURCE_REF_GROUP_A = "73900264";
    public static final String RESOURCE_REF_GROUP_B = "74900264";
    public static final String RESOURCE_REF_GROUP_C = "75900264";
    public static final String RESOURCE_REF_GROUP_D = "76900264";
    public static final String RESOURCE_REF_GROUP_E = "77900264";
    public static final String RESOURCE_REF_GROUP_F = "78900264";
    public static final String RESOURCE_REF_GROUP_G = "79900264";
    public static final String RESOURCE_REF_GROUP_MA = "76800264";
    public static final String RESOURCE_REF_GROUP_MB = "77800264";
    public static final String RESOURCE_REF_GROUP_Z = "92900264";

    // CAR RIMS
    public static final int FIELD_RANK_RIM_ASSO_REF = 2;

    // CAR SHOPS
    public static final int FIELD_RANK_DEALER_REF = 1;
    public static final int FIELD_RANK_DEALER_NAME = 2;
    public static final int FIELD_RANK_DEALER_LIBELLE = 3;
    public static final int FIELD_RANK_DEALER_SLOT_1 = 4;
    public static final int FIELD_RANK_DEALER_SLOT_15 = 18;
    public static final int DELTA_RANK_DEALER_SLOTS = 3;
    public static final String CODE_FREE_DEALER_SLOT = "61085282";
    public static final String RESOURCE_VALUE_PREFIX_FILE_NAME_CAR_DEALER = "ECD_";
    public static final String RESOURCE_VALUE_PREFIX_FILE_NAME_BIKE_DEALER = "EBD_";
    public static final String RESOURCE_VALUE_PREFIX_FILE_NAME_CAR_RENTAL = "ECR_";

    // HOUSES
    public static final int FIELD_RANK_SPOT_NAME = 2;
    public static final int FIELD_RANK_REALTOR = 3;

    // INTERIOR
    public static final int FIELD_RANK_INTERIOR_NAME = 3;
    public static final String REF_NO_INTERIOR = "11319636";
    public static final String CODE_INTERIOR_COLOR_NONE = "53364643";
    public static final String RESOURCE_REF_NONE_INTERIOR_NAME = "53365512";

    // RIMS
    public static final int FIELD_RANK_RIM_REF = 1;
    public static final int FIELD_RANK_RSC_PATH = 13;
    public static final int FIELD_RANK_RSC_FILE_NAME_FRONT = 14;
    public static final int FIELD_RANK_RSC_FILE_NAME_REAR = 15;
    public static final String RESOURCE_REF_DEFAULT_RIM_BRAND = "55765512";
    public static final String RESOURCE_REF_NO_RIM_NAME = "54276512";

    // TUTORIALS
    public static final int FIELD_RANK_VOICE_FILE = 4;

    private DatabaseConstants() {}
}
