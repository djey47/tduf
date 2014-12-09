package fr.tduf.cli.tools;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingToolTest {

    private final MappingTool mappingTool = new MappingTool();

    @Test
    public void checkArgumentsAndOptions_whenMissingOptions_shouldUseDefaultValues_andReturnTrue() throws Exception {
        // GIVEN
        String[] args = {MappingTool.Command.INFO.label};

        // WHEN
        boolean checkResult = mappingTool.checkArgumentsAndOptions(args);

        // THEN
        assertThat(checkResult).isTrue();
        assertThat(mappingTool.getBankDirectory()).isEqualTo(".");
        assertThat(mappingTool.getMapFile()).isEqualTo("." + File.separator + "Bnk1.map");
    }

    @Test
    public void checkArgumentsAndOptions_whenProvidedOptions_shouldReturnTrue() throws Exception {
        // GIVEN
        String[] args = {MappingTool.Command.INFO.label, "--bnkDir", "./bnk/", "--mapFile", "./bnk/Bnk1.map"};

        // WHEN
        boolean checkResult = mappingTool.checkArgumentsAndOptions(args);

        // THEN
        assertThat(checkResult).isTrue();
        assertThat(mappingTool.getBankDirectory()).isEqualTo("./bnk/");
        assertThat(mappingTool.getMapFile()).isEqualTo("./bnk/Bnk1.map");
    }

    @Test
    public void checkArgumentsAndOptions_whenMissingCommand_shouldReturnFalse() throws Exception {
        // GIVEN
        String[] args = {};

        // WHEN
        boolean checkResult = mappingTool.checkArgumentsAndOptions(args);

        // THEN
        assertThat(checkResult).isFalse();
    }

    @Test
    public void checkArgumentsAndOptions_whenUnknownCommand_shouldReturnFalse() throws Exception {
        // GIVEN
        String[] args = {"intox"};

        // WHEN
        boolean checkResult = mappingTool.checkArgumentsAndOptions(args);

        // THEN
        assertThat(checkResult).isFalse();
    }
}