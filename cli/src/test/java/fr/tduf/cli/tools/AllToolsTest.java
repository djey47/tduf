package fr.tduf.cli.tools;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import fr.tduf.cli.common.helper.CommandHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static fr.tduf.cli.tools.CameraTool.Command.*;
import static fr.tduf.cli.tools.DatabaseTool.Command.*;
import static fr.tduf.cli.tools.FileTool.Command.*;
import static fr.tduf.cli.tools.MappingTool.Command.*;

/**
 * Test for CLI Tools (basic use cases, no args provided)
 */
public class AllToolsTest {

    @Test
    @ExpectSystemExitWithStatus(1)
    void databaseTool_applyTdupk() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new DatabaseTool(), APPLY_TDUPK);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void databaseTool_applyPatch() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new DatabaseTool(), APPLY_PATCH);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void databaseTool_applyPatches() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new DatabaseTool(), APPLY_PATCHES);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void databaseTool_genPatch() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new DatabaseTool(), GEN_PATCH);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void databaseTool_convertPatch() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new DatabaseTool(), CONVERT_PATCH);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void databaseTool_diffPatches() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new DatabaseTool(), DIFF_PATCHES);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void fileTool_jsonify() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new FileTool(), JSONIFY);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void fileTool_applyJson() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new FileTool(), APPLYJSON);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void fileTool_decrypt() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new FileTool(), DECRYPT);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void fileTool_encrypt() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new FileTool(), ENCRYPT);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void fileTool_bankinfo() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new FileTool(), BANKINFO);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void fileTool_unpack() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new FileTool(), UNPACK);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void fileTool_repack() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new FileTool(), REPACK);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void fileTool_unpackAll() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new FileTool(), UNPACK_ALL);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void mappingTool_info() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new MappingTool(), INFO);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void mappingTool_list() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new MappingTool(), MappingTool.Command.LIST);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void mappingTool_listMissing() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new MappingTool(), LIST_MISSING);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void mappingTool_fixMissing() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new MappingTool(), FIX_MISSING);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void mappingTool_magify() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new MappingTool(), MAGIFY);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void cameraTool_list() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new CameraTool(), CameraTool.Command.LIST);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void cameraTool_copySet() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new CameraTool(), COPY_SET);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void cameraTool_copySets() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new CameraTool(), COPY_SETS);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void cameraTool_deleteSets() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new CameraTool(), DELETE_SETS);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void cameraTool_viewSet() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new CameraTool(), VIEW_SET);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void cameraTool_customizeSet() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new CameraTool(), CUSTOMIZE_SET);
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void cameraTool_useViews() throws IOException {
        // GIVEN-WHEN-THEN
        testToolCommand(new CameraTool(), USE_VIEWS);
    }

    private static void testToolCommand(GenericTool toolInstance, CommandHelper.CommandEnum command) throws IOException {

        System.out.println(String.format("> Now testing tool %s, command %s...", toolInstance.getClass().getSimpleName(), command));

        String[] commandLine = new String[]{command.getLabel()};

        toolInstance.doMain(commandLine);
    }
}
