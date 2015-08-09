package fr.tduf.gui.installer.controllers;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import fr.tduf.gui.common.helper.javafx.CommonDialogsHelper;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.steps.InstallSteps;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.control.Alert.AlertType.INFORMATION;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController implements Initializable {

    private SimpleStringProperty tduDirectoryProperty;

    @FXML
    private Parent root;

    @FXML
    private TextArea readmeTextArea;

    @FXML
    private TextField tduLocationTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initReadme();

            initActionToolbar();

        } catch (IOException e) {
            throw new RuntimeException("Window initializing failed.", e);
        }
    }

    @FXML
    public void handleUpdateMagicMapMenuItemAction(ActionEvent actionEvent) throws IOException {
        System.out.println("handleUpdateMagicMapMenuItemAction");

        if (Strings.isNullOrEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        updateMagicMap();
    }

    @FXML
    public void handleBrowseTduLocationButtonAction(ActionEvent actionEvent) {
        System.out.println("handleBrowseTduLocationButtonAction");

        browseForTduDirectory();
    }

    @FXML
    public void handleInstallButtonAction(ActionEvent actionEvent) {
        System.out.println("handleInstallButtonAction");

        if (Strings.isNullOrEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        install();
    }

    private void initReadme() throws IOException {
        File readmeFile = new File(InstallerConstants.FILE_README);

        List<String> lines = Files.readLines(readmeFile, Charset.defaultCharset());
        String readmeText = StringUtils.join(lines, System.lineSeparator());

        readmeTextArea.setText(readmeText);
    }

    private void initActionToolbar() {
        tduDirectoryProperty = new SimpleStringProperty();

        tduLocationTextField.setPromptText(DisplayConstants.PROMPT_TEXT_TDU_LOCATION);
        tduLocationTextField.textProperty().bindBidirectional(tduDirectoryProperty);
    }

    private void browseForTduDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        if(tduDirectoryProperty.getValue() != null) {
            File directory = new File(tduLocationTextField.getText());
            if (directory.exists()) {
                directoryChooser.setInitialDirectory(directory);
            }
        }

        File selectedDirectory = directoryChooser.showDialog(root.getScene().getWindow());
        if (selectedDirectory != null) {
            tduDirectoryProperty.set(selectedDirectory.getPath());
        }
    }

    private void updateMagicMap() throws IOException {

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .build();

        String magicMapFile = InstallSteps.updateMagicMapStep(configuration);
        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_MAP_UPDATE, DisplayConstants.MESSAGE_UPDATED_MAP, magicMapFile);
    }

    private void install() {

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .build();

        InstallSteps.install(configuration);
        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_INSTALL, DisplayConstants.MESSAGE_INSTALLED, "");
    }
}