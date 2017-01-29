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

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Used to apply patches to existing cameras information.
 */
public class CamPatcher {
    // TODO refactor and do not use parser directly
    private final CamerasParser camerasParser;

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
        // TODO if it does not exist, clone set first (which one?)
        List<CameraInfo.CameraView> allViews = setChangeObject.getChanges().stream()
                .map(viewChange -> {
                    // TODO transform map <VP,String> to <VP,Object> !!!
                    return CameraInfo.CameraView.fromProps(viewChange.getViewProps());
                })
                .collect(toList());

        CameraInfo updateConf = CameraInfo.builder()
                .forIdentifierAsString(setChangeObject.getId())
                .withViews(allViews)
                .build();

        CamerasHelper.updateViews(updateConf, camerasParser);
    }
}
