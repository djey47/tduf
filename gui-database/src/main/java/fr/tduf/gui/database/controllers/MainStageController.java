package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.gui.database.dto.FieldSettingsDto;
import fr.tduf.gui.database.helper.EditorLayoutHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController implements Initializable {

    private static final Class<MainStageController> thisClass = MainStageController.class;

    @FXML
    private TitledPane settingsPane;

    @FXML
    private ChoiceBox<DbResourceDto.Locale> localesChoiceBox;

    @FXML
    private ChoiceBox<String> profilesChoiceBox;

    @FXML
    private TabPane tabPane;

    @FXML
    private VBox defaultTab;

    @FXML
    private TextField databaseLocationTextField;

    @FXML
    private TextField entryNumberTextField;

    @FXML
    private Label entryItemsCountLabel;


    private List<DbDto> databaseObjects = new ArrayList<>();
    private DbDto currentTopicObject;

    private EditorLayoutDto layoutObject;
    private BulkDatabaseMiner databaseMiner;

    private Map<Integer, SimpleStringProperty> propertyByFieldRank = new HashMap<>();
    private Property<Integer> currentEntryIndexProperty;
    private Property<Integer> entryItemsCountProperty;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        try {
            initSettingsPane();

            initNavigationPane();

            // DEBUG
            databaseLocationTextField.setText("/media/DevStore/GIT/tduf/cli/integ-tests/db-json/");
        } catch (IOException e) {
            throw new RuntimeException("Window initializing failed.", e);
        }

    }

    @FXML
    public void handleLoadButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLoadButtonMouseClick");

        String databaseLocation = this.databaseLocationTextField.getText();
        if (StringUtils.isNotEmpty(databaseLocation)) {
            this.databaseObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(databaseLocation);
            this.databaseMiner = BulkDatabaseMiner.load(this.databaseObjects);

            profilesChoiceBox.setValue(profilesChoiceBox.getItems().get(0));
        }
    }

    @FXML
    public void handleNextButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleNextButtonMouseClick");

        int currentEntryIndex = currentEntryIndexProperty.getValue();
        if (currentEntryIndex >= this.currentTopicObject.getData().getEntries().size() - 1) {
            return;
        }

        currentEntryIndex++;
        this.currentEntryIndexProperty.setValue(currentEntryIndex);
        updateAllPropertiesWithItemValues(currentEntryIndex);
    }

    @FXML
    public void handlePreviousButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handlePreviousButtonMouseClick");

        int currentEntryIndex = currentEntryIndexProperty.getValue();
        if (currentEntryIndex <= 0 ) {
            return;
        }

        currentEntryIndex--;
        this.currentEntryIndexProperty.setValue(currentEntryIndex);
        updateAllPropertiesWithItemValues(currentEntryIndex);
    }

    @FXML
    public void handleFirstButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleFirstButtonMouseClick");

        this.currentEntryIndexProperty.setValue(0);
        updateAllPropertiesWithItemValues(this.currentEntryIndexProperty.getValue());
    }

    @FXML
    public void handleLastButtonMouseClick(ActionEvent actionEvent) {
        System.out.println("handleLastButtonMouseClick");

        this.currentEntryIndexProperty.setValue(this.currentTopicObject.getData().getEntries().size() - 1);
        updateAllPropertiesWithItemValues(this.currentEntryIndexProperty.getValue());
    }

    private void handleProfileChoiceChanged(String newProfileName) {
        System.out.println("handleProfileChoiceChanged: " + newProfileName);

        EditorLayoutDto.EditorProfileDto profileObject = EditorLayoutHelper.getAvailableProfileByName(newProfileName, this.layoutObject);
        fillTabPaneDynamically(profileObject);
    }

    private void initSettingsPane() throws IOException {

        this.settingsPane.setExpanded(false);

        fillLocales();

        loadAndFillProfiles();

        this.profilesChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> handleProfileChoiceChanged((String) newValue));
    }

    private void initNavigationPane() {

        this.entryItemsCountProperty = new SimpleObjectProperty<>(-1);
        this.entryItemsCountLabel.textProperty().bindBidirectional(entryItemsCountProperty, new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                if (object == -1) {
                    return "/ <?>";
                }
                return "/ " + object;
            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        });

        this.currentEntryIndexProperty = new SimpleObjectProperty<>(-1);
        this.entryNumberTextField.textProperty().bindBidirectional(currentEntryIndexProperty, new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                if (object == -1) {
                    return "<?>";
                }
                return "" + (object + 1);

            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        });

    }

    private void loadAndFillProfiles() throws IOException {
        this.layoutObject = new ObjectMapper().readValue(thisClass.getResource("/layout/defaultProfiles.json"), EditorLayoutDto.class);
        this.layoutObject.getProfiles()
                .forEach((profileObject) -> profilesChoiceBox.getItems().add(profileObject.getName()));
    }

    private void fillLocales() {
        asList(DbResourceDto.Locale.values())
                .forEach((locale) -> localesChoiceBox.getItems().add(locale));

        localesChoiceBox.setValue(DbResourceDto.Locale.UNITED_STATES);
    }

    private void fillTabPaneDynamically(EditorLayoutDto.EditorProfileDto profileObject) {

        if (databaseObjects.isEmpty()) {
            return;
        }

        DbDto.Topic startTopic = profileObject.getTopic();

        this.currentTopicObject = databaseMiner.getDatabaseTopic(startTopic).get();

        currentEntryIndexProperty.setValue(0);
        entryItemsCountProperty.setValue(this.currentTopicObject.getData().getEntries().size());

        propertyByFieldRank.clear();
        this.currentTopicObject.getStructure().getFields()

                .forEach(this::assignControl);

        updateAllPropertiesWithItemValues(this.currentEntryIndexProperty.getValue());
    }

    private void updateAllPropertiesWithItemValues(int entryIndex) {
        DbDataDto.Entry entry = this.currentTopicObject.getData().getEntries().get(entryIndex);

        entry.getItems().forEach((item) -> propertyByFieldRank.get(item.getFieldRank()).set(item.getRawValue()));
    }

    private void assignControl(DbStructureDto.Field field) {

        EditorLayoutDto.EditorProfileDto currentProfile = EditorLayoutHelper.getAvailableProfileByName(profilesChoiceBox.getValue(), layoutObject);

        Optional<FieldSettingsDto> potentialFieldSettings = currentProfile.getFieldSettings().stream()

                .filter((settings) -> settings.getName().equals(field.getName()))

                .findAny();

        String fieldName = field.getName();
        boolean fieldReadOnly = false;
        if(potentialFieldSettings.isPresent()) {
            FieldSettingsDto fieldSettings = potentialFieldSettings.get();

            if (fieldSettings.getLabel() != null) {
                fieldName = potentialFieldSettings.get().getLabel();
            }

            fieldReadOnly = fieldSettings.isReadOnly();
        }

        HBox fieldBox = createFieldBox();

        addFieldLabel(fieldBox, fieldReadOnly, fieldName);

        addTextField(fieldBox, fieldReadOnly, field.getRank());
    }

    private HBox createFieldBox() {
        HBox fieldBox = new HBox();
        fieldBox.setPrefHeight(25.0);
        fieldBox.setPadding(new Insets(5.0));
        defaultTab.getChildren().add(fieldBox);
        return fieldBox;
    }

    private void addFieldLabel(HBox fieldBox, boolean readOnly, String fieldName) {
        Label fieldNameLabel = new Label(fieldName);

        fieldNameLabel.getStyleClass().add("fieldName");
        if (readOnly) {
            fieldNameLabel.getStyleClass().add("readonlyField");
        }

        fieldNameLabel.setPrefWidth(225.0);
        fieldBox.getChildren().add(fieldNameLabel);
    }

    private void addTextField(HBox fieldBox, boolean readOnly, int fieldRank) {
        TextField fieldValue = new TextField();

        if (readOnly) {
            fieldValue.getStyleClass().add("readonlyField");
        }

        fieldValue.setPrefWidth(110.0);
        fieldValue.setEditable(!readOnly);

        SimpleStringProperty property = new SimpleStringProperty("");
        propertyByFieldRank.put(fieldRank, property);
        fieldValue.textProperty().bindBidirectional(property);

        fieldBox.getChildren().add(fieldValue);
    }
}