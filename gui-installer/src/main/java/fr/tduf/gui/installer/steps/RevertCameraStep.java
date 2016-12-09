package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.helper.VehicleSlotsHelper;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.db.patcher.domain.CustomizableCameraView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Restores previous views for vehicle.
 */
public class RevertCameraStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RevertCameraStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException, URISyntaxException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        final boolean customizedViews = Stream.of(CustomizableCameraView.values())
                .filter(cameraView -> getDatabaseContext().getPatchProperties().getCustomizedCameraView(cameraView).isPresent())
                .count() != 0;
        if (!customizedViews) {
            Log.info(THIS_CLASS_NAME, "->No customization for camera");
            return;
        }

        String cameraFileName = Paths.get(getInstallerConfiguration().resolveDatabaseDirectory(), "Cameras.bin").toString();

        long cameraId = getDatabaseContext().getPatchProperties().getCameraIdentifier().orElseGet(this::getCameraIdentifierFromDatabase);
        Log.info(THIS_CLASS_NAME, "->Reverting camera id " + cameraId + ": " + cameraFileName);
        getInstallerConfiguration().getCameraSupport().resetCamera(cameraFileName, cameraId);
    }

    // Ignore warning: method ref
    private long getCameraIdentifierFromDatabase() {
        String slotReference = getDatabaseContext().getPatchProperties().getVehicleSlotReference().<IllegalStateException>orElseThrow(() -> new IllegalStateException("Slot reference is unknown at this point. Cannot continue."));

        return VehicleSlotsHelper.load(getDatabaseContext().getMiner())
                .getVehicleSlotFromReference(slotReference)
                .map (VehicleSlot::getCameraIdentifier)
                .<IllegalStateException>orElseThrow(() -> new IllegalStateException("Vehicle slot should exist in database at this point. Cannot continue."));
    }
}
