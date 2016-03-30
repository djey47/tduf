package fr.tduf.gui.installer.steps;

import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Applies available patch onto loaded database then convert it to TDU format back
 */
public class UpdateDatabaseStep extends GenericStep {
    @Override
    protected void perform() throws IOException, ReflectiveOperationException {
        requireNonNull(getInstallerConfiguration(), "Installer configuration is required.");
        requireNonNull(getDatabaseContext(), "Database context is required.");

        final List<DbDto> topicObjects = getDatabaseContext().getTopicObjects();
        DatabasePatcher patcher = AbstractDatabaseHolder.prepare(DatabasePatcher.class, topicObjects);

        patcher.applyWithProperties(getDatabaseContext().getPatchObject(), getDatabaseContext().getPatchProperties());
    }
}
