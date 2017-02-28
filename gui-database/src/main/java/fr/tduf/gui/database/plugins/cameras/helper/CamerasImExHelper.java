package fr.tduf.gui.database.plugins.cameras.helper;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.CamPatchGenerator;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.CamPatcher;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CamerasDatabase;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.ALL;
import static fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange.fromSingleValue;

/**
 * Performs import/export operations
 */
public class CamerasImExHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param patchFile             : applied cameras patch file
     * @param camerasDatabase       : loaded cameras contents
     * @param targetSetIdentifier   : set identifier to use, can be null to use pre-existing identifier
     * @return potential path of written properties file
     */
    public Optional<String> importPatch(File patchFile, CamerasDatabase camerasDatabase, Long targetSetIdentifier) throws IOException {
        CamPatchDto patchObject = objectMapper.readValue(patchFile, CamPatchDto.class);
        overrideSetIdentifierIfNecessary(targetSetIdentifier, patchObject.getChanges());

        PatchProperties patchProperties = PatchPropertiesReadWriteHelper.readPatchProperties(patchFile);
        final PatchProperties effectiveProperties = new CamPatcher(camerasDatabase).applyWithProperties(patchObject, patchProperties);

        return PatchPropertiesReadWriteHelper.writeEffectivePatchProperties(effectiveProperties, patchFile.getAbsolutePath());
    }

    /**
     * @param patchFile             : applied cameras patch file
     * @param camerasDatabase    : loaded cameras contents
     * @param setIdentifier         : set identifier to use
     * @param viewKind              : can be null, to export all views
     */
    public void exportToPatch(File patchFile, CamerasDatabase camerasDatabase, long setIdentifier, ViewKind viewKind) throws IOException {
        ItemRange identifierRange = fromSingleValue(Long.toString(setIdentifier));
        ItemRange viewRange = viewKind == null ? ALL : fromSingleValue(viewKind.name());
        CamPatchDto camPatchObject = new CamPatchGenerator(camerasDatabase).makePatch(identifierRange, viewRange);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(patchFile, camPatchObject);
    }

    private void overrideSetIdentifierIfNecessary(Long targetSetIdentifier, List<SetChangeDto> changes) {
        if (targetSetIdentifier == null || changes.isEmpty() || changes.size() != 1) {
            return;
        }
        changes.get(0).overrideId(targetSetIdentifier.toString());
    }
}
