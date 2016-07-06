package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.common.InstallerConstants;
import fr.tduf.gui.installer.domain.exceptions.InternalStepException;
import fr.tduf.libunlimited.high.files.db.patcher.domain.PatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.high.files.db.patcher.helper.PatchPropertiesReadWriteHelper;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static fr.tduf.gui.installer.steps.GenericStep.StepType.RETRIEVE_BACKUP;
import static java.util.Objects.requireNonNull;

/**
 * Retrieve latest backup directory and update all contexts with right paths.
 */
class RetrieveBackupStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RetrieveBackupStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        Path backupRootPath = Paths.get(getInstallerConfiguration().getInstallerDirectory()).resolve(InstallerConstants.DIRECTORY_BACKUP);

        try (Stream<Path> stream = Files.walk(backupRootPath, 1)) {
            stream
                    .filter(path -> path != backupRootPath)
                    .filter(Files::isDirectory)
                    .sorted( (path1, path2) -> path2.toString().compareTo(path1.toString()) )
                    .findFirst()
                    .ifPresent(backupPath -> getInstallerConfiguration().setBackupDirectory(backupPath.toString()));
        }

        String backupDirectory = getInstallerConfiguration().getBackupDirectory();
        if (backupDirectory == null) {
            Log.info(THIS_CLASS_NAME, "->No backup found");
            throw new InternalStepException(RETRIEVE_BACKUP, DisplayConstants.MESSAGE_BACKUP_NOT_FOUND);
        }

        Log.info(THIS_CLASS_NAME, "->Using backup directory: " + backupDirectory);

        loadSnapshotPatchAndProperties(backupDirectory);
    }

    private void loadSnapshotPatchAndProperties(String backupDirectory) throws IOException {
        Path snapshotPatchFilePath = Paths.get(backupDirectory, InstallerConstants.FILE_NAME_SNAPSHOT_PATCH);
        Path effectivePatchFilePath = Paths.get(backupDirectory, InstallerConstants.FILE_NAME_EFFECTIVE_PATCH);

        DbPatchDto patchObject = new ObjectMapper().readValue(snapshotPatchFilePath.toFile(), DbPatchDto.class);

        PatchProperties patchProperties = PatchPropertiesReadWriteHelper.readPatchProperties(effectivePatchFilePath.toFile());
        if (patchProperties.isEmpty()) {
            throw new IOException(DisplayConstants.MESSAGE_INVALID_PROPERTIES);
        }

        getDatabaseContext().setPatch(patchObject, patchProperties);
    }
}
