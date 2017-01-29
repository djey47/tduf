package fr.tduf.libunlimited.high.files.bin.cameras.patcher.helper;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.common.patcher.helper.PlaceholderResolver;

import static java.util.Objects.requireNonNull;


public class CamPlaceholderResolver extends PlaceholderResolver {

    private final CamPatchDto patchObject;

    private CamPlaceholderResolver(CamPatchDto patchObject, PatchProperties patchProperties) {
        this.patchObject = patchObject;
        this.patchProperties = patchProperties;
    }

    /**
     * @param patchObject         : patch to be processed
     * @param effectiveProperties : property set for current patch object
     * @return resolver instance.
     */
    public static PlaceholderResolver load(CamPatchDto patchObject, PatchProperties effectiveProperties) {
        return new CamPlaceholderResolver(
                requireNonNull(patchObject, "Patch contents are required."),
                requireNonNull(effectiveProperties, "Patch properties are required."));
    }

    @Override
    public void resolveAllPlaceholders() {
        resolveSetIdentifierPlaceholders();

        resolvePropsValuePlaceholder();
    }

    private void resolvePropsValuePlaceholder() {

    }

    private void resolveSetIdentifierPlaceholders() {

    }
}
