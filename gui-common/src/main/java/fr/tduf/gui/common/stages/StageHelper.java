package fr.tduf.gui.common.stages;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import static fr.tduf.gui.common.ImageConstants.Resource.TDU_256;
import static java.util.Objects.requireNonNull;

/**
 * Provides features for FX stages
 */
public class StageHelper {
    /**
     * Uses standard TDU icon, 256px, for specified stage
     */
    public static void setStandardIcon(Stage stage) {
        requireNonNull(stage, "A FX stage must be provided").getIcons().clear();
        stage.getIcons().add(new Image(TDU_256.getPath()));
    }
}
