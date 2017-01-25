package fr.tduf.gui.database.plugins.cameras.helper;

import fr.tduf.gui.database.common.FxConstants;
import fr.tduf.gui.database.controllers.helper.DialogsHelper;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.database.plugins.cameras.common.FxConstants.EXTENSION_FILTER_TDUF_CAMERA_PATCH;
import static java.util.Arrays.asList;

public class CamerasDialogsHelper extends DialogsHelper {
    /**
     * Displays file load dialog for TDUF camera patch
     * @return empty if no selection was made (dismissed)
     */
    public Optional<String> askForCameraPatchLocation(Window parent) {
        List<FileChooser.ExtensionFilter> extensionFilters = asList(EXTENSION_FILTER_TDUF_CAMERA_PATCH, FxConstants.EXTENSION_FILTER_ALL);

        return askForLoadLocation(FileLocation.TDUF, extensionFilters, parent);
    }
}
