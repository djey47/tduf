package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.IOException;

import static fr.tduf.cli.tools.DatabaseTool.Command.*;
import static fr.tduf.cli.tools.FileTool.Command.*;
import static fr.tduf.cli.tools.MappingTool.Command.*;

/**
 * Test for CLI Tools (basic use cases, no args provided)
 */
public class AllToolsTest {

    @Rule
    public final ExpectedSystemExit exitRule = ExpectedSystemExit.none();

    @Test
    public void databaseTool_fix() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new DatabaseTool(), FIX);
    }

    @Test
    public void databaseTool_check() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new DatabaseTool(), CHECK);
    }

    @Test
    public void databaseTool_dump() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new DatabaseTool(), DUMP);
    }

    @Test
    public void databaseTool_gen() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new DatabaseTool(), GEN);
    }

    @Test
    public void fileTool_jsonify() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new FileTool(), JSONIFY);
    }

    @Test
    public void fileTool_applyJson() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new FileTool(), APPLYJSON);
    }

    @Test
    public void fileTool_decrypt() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new FileTool(), DECRYPT);
    }

    @Test
    public void fileTool_encrypt() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new FileTool(), ENCRYPT);
    }

    @Test
    public void fileTool_bankinfo() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new FileTool(), BANKINFO);
    }

    @Test
    public void mappingTool_info() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new MappingTool(), INFO);
    }

    @Test
    public void mappingTool_list() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new MappingTool(), LIST);
    }

    @Test
    public void mappingTool_listMissing() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new MappingTool(), LIST_MISSING);
    }

    @Test
    public void mappingTool_fixMissing() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new MappingTool(), FIX_MISSING);
    }

    private static void testToolCommand(GenericTool toolInstance, CommandHelper.CommandEnum command) throws NoSuchFieldException, IOException {

        System.out.println("> Now testing tool " + toolInstance.getClass().getSimpleName() + ", command " + command + "...");

        String[] commandLine = new String[]{command.getLabel()};

        toolInstance.doMain(commandLine);
    }
}