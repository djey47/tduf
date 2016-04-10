package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.libunlimited.high.files.banks.mapping.helper.MagicMapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.cli.tools.MappingTool.Command.*;
import static fr.tduf.libunlimited.low.files.banks.mapping.helper.MapHelper.MAPPING_FILE_NAME;
import static java.lang.Long.compare;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Command line interface for handling TDU file mapping.
 */
public class MappingTool extends GenericTool {

    @Option(name="-b", aliases = "--bnkDir", usage = "TDU banks directory (Bnk), defaults to current directory." )
    private String bankDirectory;

    @Option(name="-m", aliases = "--mapFile", usage = "Bnk1.map file, defaults to TDU banks directory\\Bnk1.map." )
    private String mapFile;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        INFO("info", "Provides general information about Bnk1.map file."),
        LIST("list", "Displays all entries in Bnk1.map file."),
        LIST_MISSING("list-missing", "Displays all files in Bnk directory which have no entry in Bnk1.map file."),
        FIX_MISSING("fix-missing", "Adds to Bnk1.map file all missing entries."),
        MAGIFY("magify", "Makes a Magic Map (=sets all entry sizes to 0.");

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

    /**
     * Utility entry point
     */
    public static void main(String[] args) throws IOException {
        new MappingTool().doMain(args);
    }

    @Override
    protected boolean commandDispatch() throws IOException {
        switch(command) {
            case INFO:
                commandResult = info(mapFile);
                break;
            case LIST:
                commandResult = list(mapFile);
                break;
            case LIST_MISSING:
                commandResult = listMissing(bankDirectory, mapFile);
                break;
            case FIX_MISSING:
                commandResult = fixMissing(bankDirectory, mapFile);
                break;
            case MAGIFY:
                commandResult = magify(mapFile);
                break;
            default:
                commandResult = null;
                return false;
        }
        return true;
    }

    @Override
    protected void assignCommand(String commandArgument) {
        command = (Command) CommandHelper.fromLabel(getCommand(), commandArgument);
    }

    @Override
    protected void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException {
        // Bnk directory: defaulted to current
        if (bankDirectory == null) {
            bankDirectory = ".";
        }

        // Map file: defaulted to current directory\Bnk1.map
        if (mapFile == null) {
            mapFile = new File(bankDirectory, MAPPING_FILE_NAME).getAbsolutePath();
        }
    }

    @Override
    protected CommandHelper.CommandEnum getCommand() {
        return LIST;
    }

    @Override
    protected List<String> getExamples() {
        return asList(
                INFO.label + " --bnkDir \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\"",
                LIST.label + " --bnkDir \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\" --mapFile \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Bnk1.map\"",
                LIST_MISSING.label + " -b \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\"",
                FIX_MISSING.label + " -b \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\" -m \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Bnk1.map\"",
                MAGIFY.label + " -m \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Bnk1.map\""
        );
    }

    private Map<String, ?> info(String sourceMapFile) throws IOException {

        BankMap map = loadBankMap(sourceMapFile);
        Collection<BankMap.Entry> mapEntries = map.getEntries();
        boolean isMagicMap = map.isMagic();

        outLine("- Bnk1.map parsing done: " + sourceMapFile);
        outLine("  -> Entry count: " + mapEntries.size());
        outLine("  -> Magic map? " + isMagicMap);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("entryCount", mapEntries.size());
        resultInfo.put("magicMap", isMagicMap);

        return resultInfo;
    }

    private Map<String, Object> list(String sourceMapFile) throws IOException {

        BankMap map = loadBankMap(sourceMapFile);
        Collection<BankMap.Entry> sortedMapEntries = map.getEntries().stream()

                .sorted((entry1, entry2) -> compare(entry1.getHash(), entry2.getHash()))

                .collect(toList());

        outLine("Bnk1.map parsing done: " + sourceMapFile);
        outLine("  -> All entries :" + sortedMapEntries);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("allEntries", sortedMapEntries);

        return resultInfo;
    }

    private Map<String, Object> listMissing(String sourceBankDirectory, String sourceMapFile) throws IOException {

        List<String> banks = MapHelper.parseBanks(sourceBankDirectory);
        Map<Long, String> checksums = MapHelper.computeChecksums(banks);

        outLine("- Bank parsing done: " + sourceBankDirectory);
        outLine("  -> File count: " + banks.size());
        outLine("  -> Files: " + banks);
        outLine("  -> Checksums: " + checksums);

        Map<Long, String> newChecksums = MapHelper.findNewChecksums(loadBankMap(sourceMapFile), checksums);

        outLine("  -> Absent from Bnk1.map: " + newChecksums);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("bankFilesFound", banks);
        resultInfo.put("checksums", checksums);
        resultInfo.put("missingChecksums", newChecksums);

        return resultInfo;
    }

    private Map<String, ?> fixMissing(String sourceBankDirectory, String sourceMapFile) throws IOException {
        MagicMapHelper.fixMagicMap(sourceMapFile, sourceBankDirectory);

        return null;
    }

    private Map<String, ?> magify(String sourceMapFile) throws IOException {
        MagicMapHelper.toMagicMap(sourceMapFile);

        return null;
    }

    private BankMap loadBankMap(String sourceMapFile) throws IOException {
        return MapParser.load(sourceMapFile).parse();
    }
}
