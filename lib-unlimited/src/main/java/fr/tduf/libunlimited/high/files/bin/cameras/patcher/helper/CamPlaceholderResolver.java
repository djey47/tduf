package fr.tduf.libunlimited.high.files.bin.cameras.patcher.helper;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.common.patcher.helper.PlaceholderResolver;

import static java.util.Objects.requireNonNull;


/**
 * Component to handle placeholder values in camera patch instructions.
 */
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
        resolveAllSetIdentifierPlaceholders();

        resolveAllPropsValuesPlaceholders();
    }

    private void resolveAllSetIdentifierPlaceholders() {
        patchObject.getChanges()
                .forEach(changeObject -> {
                    String effectiveIdentifier = resolveSimplePlaceholder(changeObject.getId());
                    changeObject.overrideId(effectiveIdentifier);
                });
    }

    private void resolveAllPropsValuesPlaceholders() {
        patchObject.getChanges().stream()
                .flatMap(setChangeObject -> setChangeObject.getChanges().stream())
                .flatMap(viewChangeDto -> viewChangeDto.getViewProps().entrySet().stream())
                .forEach(propEntry -> {
                    String effectiveValue = resolveSimplePlaceholder(propEntry.getValue());
                    propEntry.setValue(effectiveValue);
                });
    }
}
