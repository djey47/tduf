package fr.tduf.libunlimited.low.files.banks.domain;

import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway.EXTENSION_BANKS;

/**
 * Gives all mapped file kinds
 */
// TODO externalize constants
public enum MappedFileKind {
    EXT_3D("Exterior 3D model", "%s." + EXTENSION_BANKS, Paths.get("Vehicules")),
    INT_3D("Interior 3D model", "%s_I." + EXTENSION_BANKS, Paths.get("Vehicules")),
    SHOP_3D("Shop 3D model", "%s." + EXTENSION_BANKS, Paths.get("Level", "Hawai", "Spots")),
    CLOTHES_3D("Clothes 3D model", "%s." + EXTENSION_BANKS, Paths.get("Avatar", "CLOTHES")),
    FRONT_RIMS_3D("Front rims 3D model", "%s." + EXTENSION_BANKS, Paths.get("Vehicules", "Rim")),
    REAR_RIMS_3D("Rear rims 3D model", "%s." + EXTENSION_BANKS, Paths.get("Vehicules", "Rim")),
    SOUND("Engine sound", "%s_audio." + EXTENSION_BANKS, Paths.get("Sound", "Vehicules")),
    HUD("HUD (any-resolution)", "%s." + EXTENSION_BANKS, null),
    HUD_LOW("HUD (low-resolution)", "%s." + EXTENSION_BANKS, Paths.get("FrontEnd", "LowRes", "Gauges")),
    HUD_HIGH("HUD (high-resolution)", "%s." + EXTENSION_BANKS, Paths.get("FrontEnd", "HiRes", "Gauges")),
    TUTO_INSTRUCTION("Tutorial instruction", "%s.wav", Paths.get("Tutorial"));

    private final String description;
    private final String fileNameFormat;
    private final Path parentPath;

    MappedFileKind(String description, String fileNameFormat, Path parentPath) {
        this.description = description;
        this.fileNameFormat = fileNameFormat;
        this.parentPath = parentPath;
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
