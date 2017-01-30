package fr.tduf.libunlimited.high.files.bin.cameras.patcher;


import fr.tduf.libunlimited.high.files.bin.cameras.patcher.domain.CamPatchProperties;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.helper.CamPlaceholderResolver;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.helper.CamerasHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;

import java.util.List;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo.CameraView.fromPatchProps;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Used to apply patches to existing cameras information.
 */
// TODO refactor and do not use parser directly
public class CamPatcher {
    private final CamerasParser camerasParser;
    private static final long IDENTIFIER_REFERENCE_SET = 10000L;

    public CamPatcher(CamerasParser camerasParser) {
        this.camerasParser = requireNonNull(camerasParser, "Parser with loaded cameras is required");
    }

    /**
     * Execute provided patch onto current cameras information
     *
     * @return effective properties.
     */
    public PatchProperties apply(CamPatchDto patchObject) {
        return applyWithProperties(patchObject, new CamPatchProperties());
    }

    /**
     * Execute provided patch onto current database, taking properties into account.
     *
     * @return effective properties.
     */
    public PatchProperties applyWithProperties(CamPatchDto patchObject, CamPatchProperties patchProperties) {
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
        long setIdentifier = Long.valueOf(setChangeObject.getId());
        // TODO use helper method to check
        if (!camerasParser.getCameraViews().containsKey(setIdentifier)) {
            createCameraSetFromReference(setIdentifier);
        }

        List<CameraInfo.CameraView> allViews = setChangeObject.getChanges().stream()
                .map(viewChange -> fromPatchProps(viewChange.getViewProps()))
                .collect(toList());

        CameraInfo updateConf = CameraInfo.builder()
                .forIdentifier(setIdentifier)
                .withViews(allViews)
                .build();

        CamerasHelper.updateViews(updateConf, camerasParser);
    }

    private void createCameraSetFromReference(long newSetIdentifier) {
        if (!camerasParser.getCameraViews().containsKey(IDENTIFIER_REFERENCE_SET)) {
            throw new IllegalStateException("Reference camera set is unavailable: " + IDENTIFIER_REFERENCE_SET);
        }
        CamerasHelper.duplicateCameraSet(IDENTIFIER_REFERENCE_SET, newSetIdentifier, camerasParser);
    }
}
