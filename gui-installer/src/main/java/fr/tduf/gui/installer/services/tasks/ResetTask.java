package fr.tduf.gui.installer.services.tasks;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.installer.common.DisplayConstants;
import fr.tduf.gui.installer.domain.DatabaseContext;
import fr.tduf.gui.installer.domain.InstallerConfiguration;
import fr.tduf.gui.installer.domain.exceptions.StepException;
import javafx.beans.property.ObjectProperty;

public class ResetTask extends InstallerTask {
    private static final String THIS_CLASS_NAME = ResetTask.class.getSimpleName();

    public ResetTask(ObjectProperty<InstallerConfiguration> configuration, ObjectProperty<DatabaseContext> context) {
        super(configuration, context);
    }

    @Override
    protected Void call() throws StepException {
        Log.info(THIS_CLASS_NAME, "->Starting TDUCP slot reset");

        updateMessage(DisplayConstants.STATUS_RESET_IN_PROGRESS);

        // TODO
        callStepChain();

        updateMessage(DisplayConstants.STATUS_RESET_DONE);

        Log.info(THIS_CLASS_NAME, "->Done resetting");

        return null;
    }

    @Override
    protected void handleStepException(StepException se) throws StepException {
        Log.error(THIS_CLASS_NAME, "->Critical failure detected, no action will be intended yet.");

        updateMessage(DisplayConstants.STATUS_RESET_KO);

        Log.error(THIS_CLASS_NAME, "->Done resetting with error(s)");

        throw se;
    }
}
