package fr.tduf.gui.database.plugins.cameras.helper;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.CamPatcher;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Performs import/export operations
 */
public class CamerasImExHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param patchFile     : applied cameras patch file
     * @param camerasParser : loaded cameras contents
     * @return potential path of written properties file
     */
    public Optional<String> importPatch(File patchFile, CamerasParser camerasParser) throws IOException {
        CamPatchDto patchObject = objectMapper.readValue(patchFile, CamPatchDto.class);
        CamPatcher patcher = new CamPatcher(camerasParser);
        DatabasePatchProperties patchProperties = PatchPropertiesReadWriteHelper.readDatabasePatchProperties(patchFile);

        final PatchProperties effectiveProperties = patcher.applyWithProperties(patchObject, patchProperties);

//        return PatchPropertiesReadWriteHelper.writeEffectivePatchProperties(effectiveProperties, patchFile.getAbsolutePath());
        return empty();
    }
}
