package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.cli.tools.TestingTool.Command.TEST;
import static java.util.Arrays.asList;

public class TestingTool extends GenericTool {

    private Command command;

    @Option(name="-p", aliases = "--param", usage = "Allows to test mandatory parameter", required = true)
    private String requiredParam;

    enum Command implements CommandHelper.CommandEnum {
        TEST("test", "for testing purpose only"),
        TEST_U("test_u", "for testing purpose only"),
        TEST_FAIL("test_fail", "for testing purpose only");

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
        if (command == Command.TEST) {
            return true;
        }

        if(command == Command.TEST_FAIL) {
            throw new IllegalArgumentException("Exception");
        }

        return false;
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
        return asList("Example1", "Example2");
    }
}
