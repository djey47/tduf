package fr.tduf.gui.common.stages;

import fr.tduf.gui.common.ImageConstants;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static java.util.Objects.requireNonNull;

/**
 * Provides features for FX stages
 */
public class StageHelper {
    private static final Class<StageHelper> thisClass = StageHelper.class;

    /**
     * Uses standard TDU icon, 256px, for specified stage
     */
    public static void setStandardIcon(Stage stage) {
        requireNonNull(stage, "A FX stage must be provided").getIcons().clear();
        stage.getIcons().add(new Image(thisClass.getResourceAsStream(ImageConstants.RESOURCE_TDU_256)));
    }
}
