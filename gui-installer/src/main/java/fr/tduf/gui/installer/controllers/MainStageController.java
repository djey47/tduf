package fr.tduf.gui.installer.controllers;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.common.controllers.helper.DatabaseOpsHelper;
import fr.tduf.gui.common.javafx.application.AbstractGuiController;
import fr.tduf.gui.common.javafx.helper.CommonDialogsHelper;
import fr.tduf.gui.common.services.DatabaseChecker;
import fr.tduf.gui.common.services.DatabaseFixer;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.FileConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.controllers.helper.DealerSlotUserInputHelper;
import fr.tduf.gui.installer.controllers.helper.VehicleSlotUserInputHelper;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import fr.tduf.gui.installer.services.DatabaseLoader;
import fr.tduf.gui.installer.services.StepsCoordinator;
import fr.tduf.gui.installer.services.tasks.TaskType;
import fr.tduf.gui.installer.steps.GenericStep;
import fr.tduf.libunlimited.common.cache.DatabaseBanksCacheHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PlaceholderConstants;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.tduf.gui.installer.common.InstallerConstants.DIRECTORY_DATABASE;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.when;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import static javafx.scene.control.Alert.AlertType.*;

/**
 * Makes it a possible to intercept all GUI events.
 */
public class MainStageController extends AbstractGuiController {
    private static final String THIS_CLASS_NAME = MainStageController.class.getSimpleName();

    private SimpleStringProperty tduDirectoryProperty;

    private LongProperty installProgressProperty = new SimpleLongProperty();
    private BooleanProperty runningServiceProperty = new SimpleBooleanProperty();
    private DatabaseLoader databaseLoader = new DatabaseLoader();
    private DatabaseChecker databaseChecker = new DatabaseChecker();
    private DatabaseFixer databaseFixer = new DatabaseFixer();
    private StepsCoordinator stepsCoordinator = new StepsCoordinator();

    @FXML
    private TextArea readmeTextArea;

    @FXML
    private TextField tduLocationTextField;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressBar uProgressBar;

    @FXML
    private ProgressBar lProgressBar;

    @Override
    public void init() throws IOException {
        runningServiceProperty.bind(
                databaseChecker.runningProperty()
                        .or(databaseFixer.runningProperty())
                        .or(stepsCoordinator.runningProperty())
                        .or(databaseLoader.runningProperty())
        );
        mouseCursorProperty().bind(
                when(runningServiceProperty)
                        .then(Cursor.WAIT)
                        .otherwise(Cursor.DEFAULT)
        );
        uProgressBar.progressProperty().bind(installProgressProperty);
        lProgressBar.progressProperty().bind(installProgressProperty);

        initServiceListeners();

        initReadme();

        initActionToolbar();
    }

    // TODO change to private visibility
    @FXML
    public void handleUpdateMagicMapMenuItemAction() throws StepException {
        Log.trace(THIS_CLASS_NAME, "->handleUpdateMagicMapMenuItemAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        updateMagicMap();
    }

    @FXML
    public void handleResetSlotMenuItemAction() throws Exception {
        Log.trace(THIS_CLASS_NAME, "->handleResetSlotMenuItemAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        loadDatabaseForResetSlot();
    }

    @FXML
    public void handleResetDatabaseCacheMenuItemAction() throws Exception {
        Log.trace(THIS_CLASS_NAME, "->handleResetDatabaseCacheMenuItemAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        resetDatabaseCache();
    }

    @FXML
    public void handleCheckDatabaseMenuItemAction() throws Exception {
        Log.trace(THIS_CLASS_NAME, "->handleCheckDatabaseMenuItemAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        checkDatabase();
    }

    @FXML
    public void handleBrowseTduLocationButtonAction() {
        Log.trace(THIS_CLASS_NAME, "->handleBrowseTduLocationButtonAction");

        browseForTduDirectory();
    }

    @FXML
    public void handleInstallButtonAction() throws Exception {
        Log.trace(THIS_CLASS_NAME, "->handleInstallButtonAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        loadDatabaseForInstall();
    }

    @FXML
    public void handleUninstallButtonAction() throws Exception {
        Log.trace(THIS_CLASS_NAME, "->handleUninstallButtonAction");

        if (StringUtils.isEmpty(tduDirectoryProperty.getValue())) {
            return;
        }

        loadDatabaseForUninstall();
    }

    private void initReadme() throws IOException {
        Path readmePath = Paths.get(
                InstallerConstants.DIRECTORY_ASSETS,
                InstallerConstants.DIRECTORY_README,
                InstallerConstants.FILE_README);
        List<String> lines = Files.readAllLines(readmePath, Charset.defaultCharset());
        String readmeText = String.join(System.lineSeparator(), lines);

        readmeTextArea.setText(readmeText);
    }

    private void initActionToolbar() {
        tduDirectoryProperty = new SimpleStringProperty();

        tduLocationTextField.setPromptText(DisplayConstants.PROMPT_TEXT_TDU_LOCATION);
        tduLocationTextField.textProperty().bindBidirectional(tduDirectoryProperty);
    }

    private void initServiceListeners() {
        databaseChecker.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> integrityErrors = databaseChecker.integrityErrorsProperty().get();
                if (integrityErrors.isEmpty()) {
                    CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_CHECK_DB, DisplayConstants.MESSAGE_DB_CHECK_OK, DisplayConstants.MESSAGE_DB_ZERO_ERROR);
                    return;
                }
                if (DatabaseOpsHelper.displayCheckResultDialog(integrityErrors, getWindow(), DisplayConstants.TITLE_APPLICATION)) {
                    fixDatabase();
                }
            } else if (FAILED == newState) {
                handleServiceFailure(databaseChecker.exceptionProperty().get(), DisplayConstants.TITLE_SUB_CHECK_DB, DisplayConstants.MESSAGE_DB_CHECK_KO);
            }
        });

        databaseFixer.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (SUCCEEDED == newState) {
                final Set<IntegrityError> remainingErrors = databaseFixer.integrityErrorsProperty().get();
                if (remainingErrors.isEmpty()) {
                    CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_FIX_DB, DisplayConstants.MESSAGE_DB_FIX_OK, DisplayConstants.MESSAGE_DB_ZERO_ERROR);
                } else {
                    CommonDialogsHelper.showDialog(WARNING, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_FIX_DB, DisplayConstants.MESSAGE_DB_FIX_KO, DisplayConstants.MESSAGE_DB_REMAINING_ERRORS);
                }
            } else if (FAILED == newState) {
                handleServiceFailure(databaseFixer.exceptionProperty().get(), DisplayConstants.TITLE_SUB_FIX_DB, DisplayConstants.MESSAGE_DB_FIX_KO);
            }
        });

        stepsCoordinator.stateProperty().addListener((observable, oldValue, newState) -> {
            if (SUCCEEDED == newState) {
                handleCoordinatorSuccess();
            } else if (FAILED == newState) {
                handleCoordinatorFailure();
            }

            if (SUCCEEDED == newState || FAILED == newState) {
                installProgressProperty.unbind();
                installProgressProperty.setValue(0);
                statusLabel.textProperty().unbind();
            }
        });

        databaseLoader.stateProperty().addListener((observable, oldValue, newState) -> {
            DatabaseLoader.Objective objective = databaseLoader.objectiveProperty().get();
            if (SUCCEEDED == newState) {
                switch(objective) {
                    case INSTALL:
                        install(databaseLoader.getValue());
                        break;
                    case UNINSTALL:
                        uninstall(databaseLoader.getValue());
                        break;
                    case RESET_SLOT:
                        selectAndResetSlot(databaseLoader.getValue());
                        break;
                }
            } else if (FAILED == newState) {
                switch(objective) {
                    case INSTALL:
                        handleServiceFailure(databaseLoader.exceptionProperty().get(), DisplayConstants.TITLE_SUB_INSTALL, DisplayConstants.MESSAGE_DB_LOAD_KO);
                        break;
                    case UNINSTALL:
                        handleServiceFailure(databaseLoader.exceptionProperty().get(), DisplayConstants.TITLE_SUB_UNINSTALL, DisplayConstants.MESSAGE_DB_LOAD_KO);
                        break;
                    case RESET_SLOT:
                        handleServiceFailure(databaseLoader.exceptionProperty().get(), DisplayConstants.TITLE_SUB_RESET_TDUCP_SLOT, DisplayConstants.MESSAGE_DB_LOAD_KO);
                        break;
                }
            }
        });
    }

    private void browseForTduDirectory() {
        if (runningServiceProperty.get()) {
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();

        if (tduDirectoryProperty.getValue() != null) {
            File directory = new File(tduLocationTextField.getText());
            if (directory.exists()) {
                directoryChooser.setInitialDirectory(directory);
            }
        }

        File selectedDirectory = directoryChooser.showDialog(getWindow());
        if (selectedDirectory != null) {
            tduDirectoryProperty.set(selectedDirectory.getPath());
        }
    }

    private void updateMagicMap() throws StepException {
        if (runningServiceProperty.get()) {
            return;
        }

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .build();

        GenericStep.starterStep(configuration, null)
                .nextStep(GenericStep.StepType.UPDATE_MAGIC_MAP).start();

        String magicMapFile = configuration.resolveMagicMapFile();
        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_MAP_UPDATE, DisplayConstants.MESSAGE_UPDATED_MAP, magicMapFile);
    }

    private void resetDatabaseCache() throws IOException, ReflectiveOperationException {
        if (runningServiceProperty.get()) {
            return;
        }

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .build();

        Path databasePath = Paths.get(configuration.resolveDatabaseDirectory());
        DatabaseBanksCacheHelper.clearCache(databasePath);

        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESET_DB_CACHE, DisplayConstants.MESSAGE_DELETED_CACHE, databasePath.toString());
    }

    private void checkDatabase() {
        if (runningServiceProperty.get()) {
            return;
        }

        statusLabel.textProperty().bind(databaseChecker.messageProperty());

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .build();

        databaseChecker.databaseLocationProperty().setValue(configuration.resolveDatabaseDirectory());
        databaseChecker.bankSupportProperty().setValue(configuration.getBankSupport());

        databaseChecker.restart();
    }

    private void fixDatabase() {
        // Do not check for service here, as checker may still be in running state.
        statusLabel.textProperty().bind(databaseFixer.messageProperty());

        databaseFixer.fromService(databaseChecker);

        databaseFixer.restart();
    }

    // TODO create single method with objective as param
    private void loadDatabaseForInstall() throws IOException, ReflectiveOperationException {
        if (runningServiceProperty.get()) {
            return;
        }

        databaseLoader.objectiveProperty().setValue(DatabaseLoader.Objective.INSTALL);

        loadDatabase();
    }
    private void loadDatabaseForUninstall() throws IOException, ReflectiveOperationException {
        if (runningServiceProperty.get()) {
            return;
        }

        databaseLoader.objectiveProperty().setValue(DatabaseLoader.Objective.UNINSTALL);

        loadDatabase();
    }

    private void loadDatabaseForResetSlot() throws IOException, ReflectiveOperationException {
        if (runningServiceProperty.get()) {
            return;
        }

        databaseLoader.objectiveProperty().setValue(DatabaseLoader.Objective.RESET_SLOT);

        loadDatabase();
    }

    private void loadDatabase() {
        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .build();

        statusLabel.textProperty().bind(databaseLoader.messageProperty());

        databaseLoader.databaseLocationProperty().setValue(configuration.resolveDatabaseDirectory());
        databaseLoader.bankSupportProperty().setValue(configuration.getBankSupport());

        databaseLoader.restart();
    }

    private void install(DatabaseContext context) {
        // Do not check for service here, as loader may still be in running state.
        requireNonNull(context, "Database context is required. Please load database first.");

        installProgressProperty.setValue(-1);

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .build();

        try {
            loadCurrentPatch(configuration, context);
        } catch (IOException ioe) {
            StepException se = new StepException(GenericStep.StepType.LOAD_PATCH, DisplayConstants.MESSAGE_PATCH_LOAD_KO, ioe);
            handleServiceFailure(se, DisplayConstants.TITLE_SUB_INSTALL, DisplayConstants.MESSAGE_NOT_INSTALLED);

            installProgressProperty.setValue(0);
            return;
        }

        try {
            VehicleSlotUserInputHelper.selectAndDefineVehicleSlot(context, getWindow());

            DealerSlotUserInputHelper.selectAndDefineDealerSlot(context, getWindow());
        } catch (Exception e) {
            StepException se = new StepException(GenericStep.StepType.SELECT_SLOTS, DisplayConstants.MESSAGE_INSTALL_ABORTED, e);
            handleServiceFailure(se, DisplayConstants.TITLE_SUB_INSTALL, DisplayConstants.MESSAGE_NOT_INSTALLED);

            installProgressProperty.setValue(0);
            return;
        }

        statusLabel.textProperty().bind(stepsCoordinator.messageProperty());
        installProgressProperty.bind(stepsCoordinator.progressProperty());

        stepsCoordinator.configurationProperty().setValue(configuration);
        stepsCoordinator.contextProperty().setValue(context);
        stepsCoordinator.taskTypeProperty().setValue(TaskType.INSTALL);

        stepsCoordinator.restart();
    }

    private void uninstall(DatabaseContext context) {
        // Do not check for service here, as loader may still be in running state.
        requireNonNull(context, "Database context is required. Please load database first.");

        installProgressProperty.setValue(-1);

        InstallerConfiguration configuration = InstallerConfiguration.builder()
                .withTestDriveUnlimitedDirectory(tduDirectoryProperty.getValue())
                .withAssetsDirectory(InstallerConstants.DIRECTORY_ASSETS)
                .build();

        statusLabel.textProperty().bind(stepsCoordinator.messageProperty());
        installProgressProperty.bind(stepsCoordinator.progressProperty());

        stepsCoordinator.configurationProperty().setValue(configuration);
        stepsCoordinator.contextProperty().setValue(context);
        stepsCoordinator.taskTypeProperty().setValue(TaskType.UNINSTALL);

        stepsCoordinator.restart();
    }

    private void selectAndResetSlot(DatabaseContext context) {
        requireNonNull(context, "Database context is required. Please load database first.");

        VehicleSlot selectedSlot;
        try {
            selectedSlot = VehicleSlotUserInputHelper.quickSelectVehicleSlot(VehicleSlotsHelper.SlotKind.TDUCP_NEW, context, getWindow());
        } catch (Exception e) {
            StepException se = new StepException(GenericStep.StepType.SELECT_SLOTS, DisplayConstants.MESSAGE_OPERATION_ABORTED, e);
            handleServiceFailure(se, DisplayConstants.TITLE_SUB_RESET_TDUCP_SLOT, DisplayConstants.MESSAGE_NOT_RESET);
            return;
        }

        try {
            resetSlot(selectedSlot, context.getTopicObjects());
        } catch (Exception e) {
            StepException se = new StepException(GenericStep.StepType.RESTORE_SLOT, DisplayConstants.MESSAGE_RESET_SLOT_KO, e);
            handleServiceFailure(se, DisplayConstants.TITLE_SUB_RESET_TDUCP_SLOT, DisplayConstants.MESSAGE_NOT_RESET);
            return;
        }

        CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_RESET_TDUCP_SLOT, DisplayConstants.MESSAGE_RESET_SLOT, selectedSlot.getRef());
    }

    // TODO externalize to TDUCP Helper class
    private void resetSlot(VehicleSlot slot, List<DbDto> topicObjects) throws IOException, URISyntaxException, ReflectiveOperationException {

        final String slotReference = slot.getRef();

        boolean carSlotFlag;
        if (VehicleSlotsHelper.isTDUCPNewCarSlot(slotReference)) {
            carSlotFlag = true;
        } else if (VehicleSlotsHelper.isTDUCPNewBikeSlot(slotReference)) {
            carSlotFlag = false;
        } else {
            throw new IllegalArgumentException("Not a TDUCP new slot: " + slotReference);
        }

        DbPatchDto cleanSlotPatch = FilesHelper.readObjectFromJsonResourceFile(
                DbPatchDto.class,
                FileConstants.RESOURCE_NAME_CLEAN_PATCH);
        DbPatchDto resetSlotPatch = FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class,
                carSlotFlag ? FileConstants.RESOURCE_NAME_TDUCP_CAR_PATCH : FileConstants.RESOURCE_NAME_TDUCP_BIKE_PATCH);

        // TODO use format constants (create TDUCP constants class)
        String carIdentifier = Integer.toString(slot.getCarIdentifier());
        PatchProperties patchProperties = new PatchProperties();
        patchProperties.setVehicleSlotReferenceIfNotExists(slotReference);
        patchProperties.setCarIdentifierIfNotExists(carIdentifier);
        patchProperties.setResourceBankNameIfNotExists(carIdentifier + "567");
        patchProperties.register(PlaceholderConstants.PLACEHOLDER_NAME_RESOURCE_MODEL, carIdentifier + "3407");
        patchProperties.register(PlaceholderConstants.PLACEHOLDER_NAME_RESOURCE_VERSION, carIdentifier + "8427");
        patchProperties.setBankNameIfNotExists("TDUCP_" + carIdentifier);
        patchProperties.register(PlaceholderConstants.PLACEHOLDER_NAME_MODEL, "TDUCP Model " + carIdentifier);
        patchProperties.register(PlaceholderConstants.PLACEHOLDER_NAME_VERSION, "Version " + carIdentifier );

        IntStream.rangeClosed(0, 9)
                .forEach(rimRank -> {
                    patchProperties.setRimsSlotReferenceIfNotExists("0000" + carIdentifier + rimRank, rimRank);
                    patchProperties.register(
                            String.format(PlaceholderConstants.PLACEHOLDER_NAME_FMT_RESOURCE_RIM_NAME, rimRank),
                            carIdentifier + rimRank + "562");
                    patchProperties.setResourceFrontRimBankIfNotExists(carIdentifier + rimRank + "1512", rimRank);
                    patchProperties.setResourceRearRimBankIfNotExists(carIdentifier + rimRank + "2512", rimRank);
                    patchProperties.setRimNameIfNotExists("TDUCP " + carIdentifier + " - rim set " + rimRank, rimRank);
                    patchProperties.setFrontRimBankNameIfNotExists("TDUCP_" + carIdentifier + "_F_0" + rimRank, rimRank);
                    patchProperties.setRearRimBankNameIfNotExists("TDUCP_" + carIdentifier + "_R_0" + rimRank, rimRank);
                });

        IntStream.rangeClosed(0, 9)
                .forEach(pjRank -> {
                    patchProperties.setExteriorMainColorIdIfNotExists("54356127", pjRank);
                    patchProperties.setExteriorSecondaryColorIdIfNotExists("53356127", pjRank);
                    patchProperties.setCalipersColorIdIfNotExists("53356127", pjRank);
                    patchProperties.setExteriorColorNameResourceIfNotExists(carIdentifier + pjRank + "457", pjRank);
                    patchProperties.setExteriorColorNameIfNotExists("TDUCP_" + carIdentifier + " exterior color " + pjRank, pjRank);
                });

        IntStream.rangeClosed(0, 9)
                .forEach(intRank -> {
                    patchProperties.setInteriorReferenceIfNotExists(carIdentifier + intRank + "9636", intRank);
                    patchProperties.setInteriorMainColorIdIfNotExists("53364643", intRank);
                    patchProperties.setInteriorSecondaryColorIdIfNotExists("53364643", intRank);
                    patchProperties.setInteriorMaterialIdIfNotExists("53364643", intRank);
                });

        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, topicObjects);
        patcher.applyWithProperties(cleanSlotPatch, patchProperties);
        patcher.applyWithProperties(resetSlotPatch, patchProperties);
    }


    private void handleCoordinatorFailure() {
        if (TaskType.UNINSTALL == stepsCoordinator.taskTypeProperty().getValue()) {
            handleServiceFailure(stepsCoordinator.exceptionProperty().get(), DisplayConstants.TITLE_SUB_UNINSTALL, DisplayConstants.MESSAGE_NOT_UNINSTALLED);
        } else if (TaskType.INSTALL == stepsCoordinator.taskTypeProperty().getValue()) {
            handleServiceFailure(stepsCoordinator.exceptionProperty().get(), DisplayConstants.TITLE_SUB_INSTALL, DisplayConstants.MESSAGE_NOT_INSTALLED);
        }
    }

    private void handleCoordinatorSuccess() {
        if (TaskType.UNINSTALL == stepsCoordinator.taskTypeProperty().getValue()) {
            CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_UNINSTALL, DisplayConstants.MESSAGE_UNINSTALLED, "");
        } else if (TaskType.INSTALL == stepsCoordinator.taskTypeProperty().getValue()) {
            CommonDialogsHelper.showDialog(INFORMATION, DisplayConstants.TITLE_APPLICATION + DisplayConstants.TITLE_SUB_INSTALL, DisplayConstants.MESSAGE_INSTALLED, "");
        }
    }

    private static void loadCurrentPatch(InstallerConfiguration configuration, DatabaseContext context) throws IOException {
        Path patchPath = Paths.get(configuration.getAssetsDirectory(), DIRECTORY_DATABASE);

        File patchFile;
        try (Stream<Path> pathStream = Files.walk(patchPath, 1)) {
            patchFile = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> EXTENSION_JSON.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))
                    .findFirst()
                    .orElseThrow(() -> new IOException(String.format(DisplayConstants.MESSAGE_FMT_PATCH_NOT_FOUND, DIRECTORY_DATABASE)))
                    .toFile();
        }

        DbPatchDto patchObject = new ObjectMapper().readValue(patchFile, DbPatchDto.class);

        PatchProperties patchProperties = PatchPropertiesReadWriteHelper.readPatchProperties(patchFile);
        if (patchProperties.isEmpty()) {
            throw new IOException(DisplayConstants.MESSAGE_INVALID_PROPERTIES);
        }

        context.setPatch(patchObject, patchProperties);
    }

    private static void handleServiceFailure(Throwable throwable, String subTitle, String mainMessage) {
        Log.error(THIS_CLASS_NAME, ExceptionUtils.getStackTrace(throwable));

        String stepName = DisplayConstants.LABEL_STEP_UNKNOWN;
        if (throwable instanceof StepException) {
            stepName = ((StepException) throwable).getStepName();
        }

        String causeMessage = "";
        if (throwable.getCause() != null
                && throwable.getCause() != throwable) {
            causeMessage = throwable.getCause().getMessage();
        }

        final String errorMessage = String.format(DisplayConstants.MESSAGE_FMT_ERROR, throwable.getMessage(), causeMessage, stepName);
        CommonDialogsHelper.showDialog(ERROR, DisplayConstants.TITLE_APPLICATION + subTitle, mainMessage, errorMessage);
    }
}
