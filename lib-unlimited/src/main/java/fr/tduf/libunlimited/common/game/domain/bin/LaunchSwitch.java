package fr.tduf.libunlimited.common.game.domain.bin;

/**
 * All switches to be used at launch
 */
public enum LaunchSwitch {
    // TODO add PP's switches
    WINDOWED("w", "Start windowed", true),
    FRAMERATE("fps", "Show FPS in upper-right corner", true),
    POSITION("pos", "Show coordinates, cameras and sectors in upper-left corner", true),
    TRAFFIC("???", "Show traffic position on GPS map", true), //TODO find code
    FUEL_PP("fuel", "Enable FUEL mode", false),
    HD_PP("hd", "Enable High Definition support", false);

    private static final String SUFFIX_PROJECT_PARADISE = "_PP";
    
    private final String code;
    private final String label;
    private final boolean genuine;

    LaunchSwitch(String code, String label, boolean genuine) {
        this.code = code;
        this.label = label;
        this.genuine = genuine;
    }

    /**
     * @return true if this is a switch brought by Project Paradise mod, false otherwise
     */
    public boolean isFromProjectParadise() {
        return name().endsWith(SUFFIX_PROJECT_PARADISE);
    }
    
    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isGenuine() {
        return genuine;
    }
}
