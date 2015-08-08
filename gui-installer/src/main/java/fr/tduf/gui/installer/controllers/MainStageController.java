package fr.tduf.gui.installer.controllers;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import fr.tduf.gui.installer.Installer;
import fr.tduf.gui.installer.common.InstallerConstants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController implements Initializable {

    @FXML
    private Parent root;

    @FXML
    private TextArea readmeTextArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initReadme();
        } catch (IOException e) {
            throw new RuntimeException("Window initializing failed.", e);
        }
    }

    private void initReadme() throws IOException {
        File readmeFile = new File(InstallerConstants.FILE_README);

        List<String> lines = Files.readLines(readmeFile, Charset.defaultCharset());
        String readmeText = StringUtils.join(lines, System.lineSeparator());

        readmeTextArea.setText(readmeText);
    }
}