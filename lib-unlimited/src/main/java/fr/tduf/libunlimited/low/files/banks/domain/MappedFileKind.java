package fr.tduf.libunlimited.low.files.banks.domain;

import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.libunlimited.common.game.FileConstants.*;
import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.EXTENSION_BANKS;

/**
 * Gives all mapped file kinds
 */
public enum MappedFileKind {
    EXT_3D("Exterior 3D model", FORMAT_REGULAR_BANK, DIRECTORY_VEHICLES),
    INT_3D("Interior 3D model", FORMAT_VEHICLE_INT_BANK, DIRECTORY_VEHICLES),
    HOUSE_EXT_3D("Exterior 3D model", FORMAT_REGULAR_BANK, DIRECTORY_LEVEL, DIRECTORY_HAWAI, DIRECTORY_SPOTS),
    HOUSE_LOUNGE_3D("Lounge 3D model", FORMAT_REGULAR_BANK, DIRECTORY_INTERIOR),
    HOUSE_GARAGE_3D("Garage 3D model", FORMAT_REGULAR_BANK, DIRECTORY_INTERIOR),
    SHOP_EXT_3D("Exterior 3D model", FORMAT_REGULAR_BANK, DIRECTORY_LEVEL, DIRECTORY_HAWAI, DIRECTORY_SPOTS),
    SHOP_INT_3D("Interior 3D model", FORMAT_REGULAR_BANK, DIRECTORY_INTERIOR),
    REALTOR_EXT_3D("Exterior 3D model", FORMAT_REGULAR_BANK, DIRECTORY_LEVEL, DIRECTORY_HAWAI, DIRECTORY_SPOTS),
    REALTOR_INT_3D("Interior 3D model", FORMAT_REGULAR_BANK, DIRECTORY_INTERIOR),
    SPOT_MAP_SCREEN("Spot Map Thumbnail", FORMAT_REGULAR_BANK, DIRECTORY_FRONT_END, DIRECTORY_ALL_RES, DIRECTORY_MAP_SCREENS, DIRECTORY_SPOTS),
    CLOTHES_3D("Clothes 3D model", FORMAT_REGULAR_BANK, DIRECTORY_AVATAR, DIRECTORY_CLOTHES),
    FRONT_RIMS_3D("Front rims 3D model", FORMAT_REGULAR_BANK, DIRECTORY_VEHICLES, DIRECTORY_RIM),
    REAR_RIMS_3D("Rear rims 3D model", FORMAT_REGULAR_BANK, DIRECTORY_VEHICLES, DIRECTORY_RIM),
    SOUND("Engine sound", "%s_audio." + EXTENSION_BANKS, DIRECTORY_SOUNDS, DIRECTORY_VEHICLES),
    HUD("HUD (any-resolution)", FORMAT_REGULAR_BANK, null),
    HUD_LOW("HUD (low-resolution)", FORMAT_REGULAR_BANK, DIRECTORY_FRONT_END, DIRECTORY_LOW_RES, DIRECTORY_HUDS),
    HUD_HIGH("HUD (high-resolution)", FORMAT_REGULAR_BANK, DIRECTORY_FRONT_END, DIRECTORY_HI_RES, DIRECTORY_HUDS),
    TUTO_INSTRUCTION("Tutorial instruction", FORMAT_REGULAR_SOUND, DIRECTORY_TUTORIAL);

    private final String description;
    private final String fileNameFormat;
    private final Path parentPath;

    MappedFileKind(String description, String fileNameFormat, String parentPath, String... morePath) {
        this.description = description;
        this.fileNameFormat = fileNameFormat;
        this.parentPath = parentPath == null ? null : Paths.get(parentPath, morePath);
    }

    public String getDescription() {
        return description;
    }

    public String getFileNameFormat() {
        return fileNameFormat;
    }

    public Path getParentPath() {
        return parentPath;
    }
}
