package fr.tduf.cli.tools;

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
    public void databaseTool_check() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new DatabaseTool(), CHECK.getLabel());
    }

    @Test
    public void databaseTool_dump() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(0);

        testToolCommand(new DatabaseTool(), DUMP.getLabel());
    }

    @Test
    public void databaseTool_gen() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(0);

        testToolCommand(new DatabaseTool(), GEN.getLabel());
    }

    @Test
    public void fileTool_jsonify() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new FileTool(), JSONIFY.getLabel());
    }

    @Test
    public void fileTool_applyJson() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new FileTool(), APPLYJSON.getLabel());
    }

    @Test
    public void fileTool_decrypt() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new FileTool(), DECRYPT.getLabel());
    }

    @Test
    public void fileTool_encrypt() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new FileTool(), ENCRYPT.getLabel());
    }

    @Test
    public void mappingTool_info() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new MappingTool(), INFO.getLabel());
    }

    @Test
    public void mappingTool_list() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new MappingTool(), LIST.getLabel());
    }

    @Test
    public void mappingTool_listMissing() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new MappingTool(), LIST_MISSING.getLabel());
    }

    @Test
    public void mappingTool_fixMissing() throws NoSuchFieldException, IOException {
        // GIVEN-WHEN-THEN
        exitRule.expectSystemExitWithStatus(1);

        testToolCommand(new MappingTool(), FIX_MISSING.getLabel());
    }

    private static void testToolCommand(GenericTool toolInstance, String commandLabel) throws NoSuchFieldException, IOException {

        System.out.println("> Now testing tool " + toolInstance.getClass().getSimpleName() + ", command " + commandLabel + "...");

        String[] commandLine = new String[]{commandLabel};

        toolInstance.doMain(commandLine);
    }
}