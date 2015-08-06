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
        FIX_MISSING("fix-missing", "Adds to Bnk1.map file all missing entries.");

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
                info();
                break;
            case LIST:
                list();
                break;
            case LIST_MISSING:
                listMissing();
                break;
            case FIX_MISSING:
                fixMissing();
                break;
            default:
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
                FIX_MISSING.label + " -b \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\" -m \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Bnk1.map\""
        );
    }

    private void info() throws IOException {

        outLine("- BNK root folder: " + this.bankDirectory);

        BankMap map = loadBankMap();
        Collection<BankMap.Entry> mapEntries = map.getEntries();

        outLine("- Bnk1.map parsing done: " + this.mapFile);
        outLine("  -> Entry count: " + mapEntries.size());

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("entryCount", mapEntries.size());
        commandResult = resultInfo;
    }

    private void list() throws IOException {

        BankMap map = loadBankMap();
        Collection<BankMap.Entry> sortedMapEntries = map.getEntries().stream()

                .sorted((entry1, entry2) -> compare(entry1.getHash(), entry2.getHash()))

                .collect(toList());

        outLine("Bnk1.map parsing done: " + this.mapFile);
        outLine("  -> All entries :" + sortedMapEntries);

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("allEntries", sortedMapEntries);
        commandResult = resultInfo;
    }

    private void listMissing() throws IOException {

        List<String> banks = MapHelper.parseBanks(this.bankDirectory);
        Map<Long, String> checksums = MapHelper.computeChecksums(banks);

        outLine("- Bank parsing done: " + this.bankDirectory);
        outLine("  -> File count: " + banks.size());
        outLine("  -> Files: " + banks);
        outLine("  -> Checksums: " + checksums);

        Map<Long, String> newChecksums = MapHelper.findNewChecksums(loadBankMap(), checksums);

        outLine("  -> Absent from Bnk1.map: " + newChecksums);

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("bankFilesFound", banks);
        resultInfo.put("checksums", checksums);
        resultInfo.put("missingChecksums", newChecksums);
        commandResult = resultInfo;
    }

    private void fixMissing() throws IOException {
        MagicMapHelper.fixMagicMap(this.mapFile, this.bankDirectory);

        outLine("Bnk1.map fixing done: " + this.mapFile);
    }

    private BankMap loadBankMap() throws IOException {
        return MapParser.load(this.mapFile).parse();
    }
}