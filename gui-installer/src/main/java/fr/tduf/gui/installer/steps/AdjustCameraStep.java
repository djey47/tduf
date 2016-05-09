package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;

/**
 * Customizes views for installed vehicle.
 */
class AdjustCameraStep extends GenericStep {
    private static final String THIS_CLASS_NAME = AdjustCameraStep.class.getSimpleName();

    private static final String REGEX_SEPARATOR_CAM_VIEW = "\\|";

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        int cameraId = getDatabaseContext().getPatchProperties().getCameraIdentifier().orElseGet(this::getCameraIdentifierFromDatabase);
        String cameraFileName = Paths.get(getInstallerConfiguration().resolveDatabaseDirectory(), "Cameras.bin").toString();
        GenuineCamViewsDto customViewsObject = buildCustomViewsFromProperties();

        if(customViewsObject.getViews().isEmpty()) {
            Log.info(THIS_CLASS_NAME, "->No customization for camera id " + cameraId);
        } else {
            Log.info(THIS_CLASS_NAME, "->Adjusting camera id " + cameraId + ": " + cameraFileName);
            getInstallerConfiguration().getCameraSupport().customizeCamera(cameraFileName, cameraId, customViewsObject);
        }
    }

    private GenuineCamViewsDto buildCustomViewsFromProperties() {
        final GenuineCamViewsDto genuineCamViewsDto = new GenuineCamViewsDto();

        Stream.of(PatchProperties.CustomizableCameraView.values())

                .map(this::buildCustomViewFromProperties)

                .filter(Optional::isPresent)

                .map(Optional::get)

                .collect(toCollection(genuineCamViewsDto::getViews));

        return genuineCamViewsDto;
    }

    private Optional<GenuineCamViewsDto.GenuineCamViewDto> buildCustomViewFromProperties(PatchProperties.CustomizableCameraView cameraView) {

        return getDatabaseContext().getPatchProperties().getCustomizedCameraView(cameraView)

                .map(prop -> {
                    final String[] camCompounds = prop.split(REGEX_SEPARATOR_CAM_VIEW);
                    if (camCompounds.length != 2) {
                        throw new IllegalArgumentException("Camera view format is not valid: " + prop);
                    }

                    final GenuineCamViewsDto.GenuineCamViewDto genuineCamViewDto = new GenuineCamViewsDto.GenuineCamViewDto();

                    genuineCamViewDto.setViewType(cameraView.getGenuineViewType());
                    genuineCamViewDto.setCameraId(Integer.parseInt(camCompounds[0]));
                    genuineCamViewDto.setViewId(Integer.parseInt(camCompounds[1]));

                    return genuineCamViewDto;
                });
    }

    private int getCameraIdentifierFromDatabase() {
        String slotReference = getDatabaseContext().getPatchProperties().getVehicleSlotReference().orElseThrow(() -> new IllegalStateException("Slot reference is unknown at this point. Cannot continue."));

        return VehicleSlotsHelper.load(getDatabaseContext().getMiner())
                .getVehicleSlotFromReference(slotReference)
                .map (VehicleSlot::getCameraIdentifier)
                .orElseThrow(() -> new IllegalStateException("Vehicle slot should exist in database at this point. Cannot continue."));
    }
}
