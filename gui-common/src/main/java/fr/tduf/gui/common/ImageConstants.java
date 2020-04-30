package fr.tduf.gui.common;

import java.io.InputStream;

public class ImageConstants {
    private static final Class<ImageConstants> thisClass = ImageConstants.class;

    private static final String RESOURCE_PATH_IMAGES = "/gui-common/img/fwdw/";
    private static final String RESOURCE_PATH_ICONS = "/gui-common/img/icons/";

    // Sizes

    public static final double SIZE_BUTTON_PICTO = 12.0;
    public static final double SIZE_DEFAULT_PICTO = 24.0;

    /**
     * Resource list and paths
     */
    public enum Resource {
        /**
         * RED WHITE CROSS SIGN
         */
        ERROR(ResourceType.PICTO, "001_05.png"),

        /**
         * BLUE EMPTY BOX SIGN
         */
        BOX_EMPTY_BLUE(ResourceType.PICTO, "001_07.png"),

        /**
         * ORANGE WARNING SIGN
         */
        WARN_ORANGE(ResourceType.PICTO, "001_11.png"),

        /**
         * RED WARNING SIGN
         */
        WARN_RED(ResourceType.PICTO, "001_30.png"),

        /**
         * BLUE MAGNIFIER SIGN
         */
        MAGNIFIER_BLUE(ResourceType.PICTO, "001_38.png"),

        /**
         * TDU ICON 256px
         */
        TDU_256(ResourceType.ICON, "TDU-256px.png");

        private final String fileName;
        private final ResourceType type;

        Resource(ResourceType type, String fileName) {
            this.type = type;
            this.fileName = fileName;
        }

        /**
         * @return resource path
         */
        public String getPath() {
            return type.pathPrefix + fileName;
        }

        /**
         * @return resource input stream
         */
        public InputStream getStream() {
            return thisClass.getResourceAsStream(getPath());
        }
    }

    private enum ResourceType {
        PICTO(RESOURCE_PATH_IMAGES),
        ICON(RESOURCE_PATH_ICONS);

        private final String pathPrefix;

        ResourceType(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }
    }
}
