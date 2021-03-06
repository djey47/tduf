package fr.tduf.cli.tools;

import com.esotericsoftware.minlog.Log;
import fr.tduf.cli.common.helper.CommandHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.cli.tools.TestingTool.Command.TEST;
import static java.util.Arrays.asList;

public class TestingTool extends GenericTool {

    private Command command;

    @Option(name="-p", aliases = "--param", usage = "Allows to test mandatory parameter", required = true)
    private String requiredParam;

    enum Command implements CommandHelper.CommandEnum {
        TEST("test", "for testing purpose only"),
        TEST_U("test_u", "for testing purpose only"),
        TEST_FAIL("test_fail", "for testing purpose only"),
        TEST_OUTLINE("test_outline", "for testing purpose only"),
        TEST_RESULT("test_result", "for testing purpose only");

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

        switch (command) {
            case TEST:
                break;
            case TEST_FAIL:
                throw new IllegalArgumentException("Exception");
            case TEST_OUTLINE:
                commandResult = outline();
                break;
            case TEST_RESULT:
                commandResult = result();
                break;
            default:
                commandResult = null;
                return false;
        }

        return true;
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

    private Map<String, ?> outline() {
        outLine(requiredParam);

        return null;
    }

    private Map<String, Object> result() {
        Log.info(TestingTool.class.getSimpleName(), "");
        Log.debug(TestingTool.class.getSimpleName(), "This is for sake of verbosity.");

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("result", "ok");

        return resultInfo;
    }
}
