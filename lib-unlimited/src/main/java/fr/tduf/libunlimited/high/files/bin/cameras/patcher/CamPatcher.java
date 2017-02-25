package fr.tduf.libunlimited.high.files.bin.cameras.patcher;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.helper.CamPlaceholderResolver;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfoEnhanced;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;

import java.util.List;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo.CameraView.fromPatchProps;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Used to apply patches to existing cameras information.
 */
public class CamPatcher {
    private final CameraInfoEnhanced cameraInfoEnhanced;
    private static final int IDENTIFIER_REFERENCE_SET = 10000;

    public CamPatcher(CameraInfoEnhanced cameraInfoEnhanced) {
        this.cameraInfoEnhanced = requireNonNull(cameraInfoEnhanced, "Loaded cameras info is required");
    }

    /**
     * Execute provided patch onto current cameras information
     *
     * @return effective properties.
     */
    public PatchProperties apply(CamPatchDto patchObject) {
        return applyWithProperties(patchObject, new PatchProperties());
    }

    /**
     * Execute provided patch onto current database, taking properties into account.
     *
     * @return effective properties.
     */
    public PatchProperties applyWithProperties(CamPatchDto patchObject, PatchProperties patchProperties) {
        requireNonNull(patchObject, "A patch object is required.");
        requireNonNull(patchProperties, "Patch properties are required.");

        PatchProperties effectiveProperties = patchProperties.makeCopy();

        CamPlaceholderResolver
                .load(patchObject, effectiveProperties)
                .resolveAllPlaceholders();

        patchObject.getChanges()
                .forEach(this::applyChange);

        return effectiveProperties;
    }

    private void applyChange(SetChangeDto setChangeObject) {
        int setIdentifier = Integer.valueOf(setChangeObject.getId());
        if (!CamerasHelper.cameraSetExists(setIdentifier, cameraInfoEnhanced)) {
            createCameraSetFromReference(setIdentifier);
        }

        List<CameraInfo.CameraView> allViews = setChangeObject.getChanges().stream()
                .map(viewChange -> fromPatchProps(viewChange.getViewProps(), viewChange.getCameraViewKind()))
                .collect(toList());

        CameraInfo updateConf = CameraInfo.builder()
                .forIdentifier(setIdentifier)
                .withViews(allViews)
                .build();

        CamerasHelper.updateViews(updateConf, cameraInfoEnhanced);
    }

    private void createCameraSetFromReference(int newSetIdentifier) {
        if (!cameraInfoEnhanced.cameraSetExistsInSettings(IDENTIFIER_REFERENCE_SET)) {
            throw new IllegalStateException("Reference camera set is unavailable: " + IDENTIFIER_REFERENCE_SET);
        }
        CamerasHelper.duplicateCameraSet(IDENTIFIER_REFERENCE_SET, newSetIdentifier, cameraInfoEnhanced);
    }
}
