package fr.tduf.gui.installer.steps;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;

import java.io.IOException;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

/**
 * Apply snapshot patch using effective properties to restore database entries
 */
public class RestoreSnapshotStep extends GenericStep {
    private static final String THIS_CLASS_NAME = RestoreSnapshotStep.class.getSimpleName();

    @Override
    protected void perform() throws IOException, ReflectiveOperationException, URISyntaxException {
        requireNonNull(getDatabaseContext(), "Database context is required.");
        requireNonNull(getDatabaseContext().getPatchObject(), "Snapshot is required.");
        requireNonNull(getDatabaseContext().getPatchProperties(), "Effective patch properties are required.");

        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, getDatabaseContext().getTopicObjects());

        Log.info(THIS_CLASS_NAME, "->Restoring latest SNAPSHOT...");
        patcher.applyWithProperties(getDatabaseContext().getPatchObject(), getDatabaseContext().getPatchProperties());
    }
}
