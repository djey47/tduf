package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.db.patcher.domain.CustomizableCameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraView;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.dto.SetConfigurationDto;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Customizes views for installed vehicle.
 */
class AdjustCameraStep extends GenericStep {
    private static final String THIS_CLASS_NAME = AdjustCameraStep.class.getSimpleName();

    private static final String REGEX_SEPARATOR_CAM_VIEW = "\\|";

    private VehicleSlotsHelper vehicleSlotsHelper;

    @Override
    protected void onInit() {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        vehicleSlotsHelper = VehicleSlotsHelper.load(getDatabaseContext().getMiner());
    }

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        long cameraId = getDatabaseContext().getPatchProperties().getCameraIdentifier()
                .orElseGet(this::getCameraIdentifierFromDatabase);
        String cameraFileName = Paths.get(getInstallerConfiguration().resolveDatabaseDirectory(), CamerasHelper.FILE_CAMERAS_BIN).toString();
        SetConfigurationDto config = buildCustomViewsFromProperties(Long.valueOf(cameraId).intValue());

        if(config.getViews().isEmpty()) {
            Log.info(THIS_CLASS_NAME, "->No customization for camera id " + cameraId);
        } else {
            Log.info(THIS_CLASS_NAME, "->Adjusting camera id " + cameraId + ": " + cameraFileName);
            CamerasHelper.useViews(config, cameraFileName);
        }
    }

    private SetConfigurationDto buildCustomViewsFromProperties(int cameraIdentifier) {
        List<CameraView> views = Stream.of(CustomizableCameraView.values())
                .map(this::buildCustomViewFromProperties)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        return SetConfigurationDto.builder()
                .forIdentifier(cameraIdentifier)
                .withViews(views)
                .build();
    }

    private Optional<CameraView> buildCustomViewFromProperties(CustomizableCameraView cameraView) {
        return getDatabaseContext().getPatchProperties().getCustomizedCameraView(cameraView)
                .map(prop -> {
                    final String[] camCompounds = prop.split(REGEX_SEPARATOR_CAM_VIEW);
                    if (camCompounds.length != 2) {
                        throw new IllegalArgumentException("Camera view format is not valid: " + prop);
                    }

                    ViewKind viewType = cameraView.getGenuineViewType();
                    ViewKind sourceViewType = CustomizableCameraView.fromSuffix(camCompounds[1]).getGenuineViewType();
                    int sourceCameraIdentifier = Integer.valueOf(camCompounds[0]);
                    return CameraView.from(
                            viewType,
                            sourceCameraIdentifier,
                            sourceViewType);
                });
    }

    private long getCameraIdentifierFromDatabase() {
        String slotReference = getDatabaseContext().getPatchProperties().getVehicleSlotReference()
                .orElseThrow(() -> new IllegalStateException("Slot reference is unknown at this point. Cannot continue."));

        return vehicleSlotsHelper.getVehicleSlotFromReference(slotReference)
                .map (VehicleSlot::getCameraIdentifier)
                .orElseThrow(() -> new IllegalStateException("Vehicle slot should exist in database at this point. Cannot continue."));
    }

    // For testing use
    void setVehicleSlotsHelper(VehicleSlotsHelper vehicleSlotsHelper) {
        this.vehicleSlotsHelper = vehicleSlotsHelper;
    }
}
