package fr.tduf.libunlimited.high.files.bin.cameras.patcher;


import fr.tduf.libunlimited.high.files.bin.cameras.patcher.domain.CamPatchProperties;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.helper.CamPlaceholderResolver;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Used to apply patches to existing cameras information.
 */
public class CamPatcher {
    private final List<CameraInfo> camerasInformation;

    public CamPatcher(List<CameraInfo> camerasInformation) {
        this.camerasInformation = requireNonNull(camerasInformation, "Loaded cameras information is required");
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

    }

}
