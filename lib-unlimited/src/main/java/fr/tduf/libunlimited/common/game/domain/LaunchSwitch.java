package fr.tduf.libunlimited.common.game.domain;

/**
 * All switches to be used at launch
 */
public enum LaunchSwitch {
    WINDOWED("w", "Start windowed"),
    FRAMERATE("fps", "Show FPS in upper-right corner"),
    POSITION("pos", "Show coordinates, cameras and sectors in upper-left corner"),
    TRAFFIC("???", "Show traffic position on GPS map"), //TODO find code
    FUEL_PP("fuel", "Enable FUEL mode"),
    HD_PP("hd", "Enable High Definition overrides");

    private final String code;
    private final String label;

    LaunchSwitch(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
