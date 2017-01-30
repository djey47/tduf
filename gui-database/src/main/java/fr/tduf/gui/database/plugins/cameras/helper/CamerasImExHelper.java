package fr.tduf.gui.database.plugins.cameras.helper;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.CamPatcher;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Performs import/export operations
 */
public class CamerasImExHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param patchFile             : applied cameras patch file
     * @param camerasParser         : loaded cameras contents
     * @param targetSetIdentifier   : set identifier to use, can be null to use pre-existing identifier
     * @return potential path of written properties file
     */
    public Optional<String> importPatch(File patchFile, CamerasParser camerasParser, Long targetSetIdentifier) throws IOException {
        CamPatchDto patchObject = objectMapper.readValue(patchFile, CamPatchDto.class);

        overrideSetIdentifierIfNecessary(targetSetIdentifier, patchObject.getChanges());

        CamPatcher patcher = new CamPatcher(camerasParser);
        PatchProperties patchProperties = PatchPropertiesReadWriteHelper.readPatchProperties(patchFile);

        final PatchProperties effectiveProperties = patcher.applyWithProperties(patchObject, patchProperties);

        return PatchPropertiesReadWriteHelper.writeEffectivePatchProperties(effectiveProperties, patchFile.getAbsolutePath());
    }

    private void overrideSetIdentifierIfNecessary(Long targetSetIdentifier, List<SetChangeDto> changes) {
        if (targetSetIdentifier == null || changes.isEmpty() || changes.size() != 1) {
            return;
        }
        changes.get(0).overrideId(targetSetIdentifier.toString());
    }
}
