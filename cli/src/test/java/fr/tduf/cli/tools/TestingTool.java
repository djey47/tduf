package fr.tduf.cli.tools;

import fr.tduf.cli.common.CommandHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.cli.tools.TestingTool.Command.TEST;

public class TestingTool extends GenericTool {

    private Command command;

    enum Command implements CommandHelper.CommandEnum {
        TEST("test", "for testing purpose only"),
        TEST_U("test_u", "for testing purpose only");

        final String label;
        final String description;

        Command(String label, String description) {
            this.label = label;
            this.description = description;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public CommandHelper.CommandEnum[] getValues() {
            return values();
        }

    }

    @Override
    protected boolean commandDispatch() throws IOException {
        return (command == Command.TEST);
    }

    @Override
    protected void assignCommand(String commandArgument) {
        this.command = (Command) CommandHelper.fromLabel(getCommand(), commandArgument);
    }

    @Override
    protected void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException {
        if(command == Command.TEST_U) {
            throw new CmdLineException(parser, "Error", null);
        }
    }

    @Override
    protected CommandHelper.CommandEnum getCommand() {
        return TEST;
    }

    @Override
    protected List<String> getExamples() {
        return new ArrayList<>();
    }
}
