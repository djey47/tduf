package fr.tduf.cli.tools;

import fr.tduf.cli.common.CommandHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.MapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.parser.MapParser;
import fr.tduf.libunlimited.low.files.banks.mapping.writer.MapWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static fr.tduf.cli.tools.MappingTool.Command.*;
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
            // TODO use File with absolutepath instead
            mapFile = bankDirectory + File.separator + "Bnk1.map";
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
                LIST.label + " --bnkDir \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\" --mapFile \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\"Bnk1.map",
                LIST_MISSING.label + " -b \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\"",
                FIX_MISSING.label + " -b \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\" -m \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\"Bnk1.map\""
        );
    }

    private void info() throws IOException {

        System.out.println("- BNK root folder: " + this.bankDirectory);

        BankMap map = loadBankMap();
        Collection<BankMap.Entry> mapEntries = map.getEntries();

        System.out.println("- Bnk1.map parsing done: " + this.mapFile);
        System.out.println("  -> Entry count: " + mapEntries.size());
    }

    private void list() throws IOException {

        BankMap map = loadBankMap();
        Collection<BankMap.Entry> sortedMapEntries = map.getEntries().stream()

                .sorted((entry1, entry2) -> compare(entry1.getHash(), entry2.getHash()))

                .collect(toList());

        System.out.println("Bnk1.map parsing done: " + this.mapFile);
        System.out.println("  -> All entries :" + sortedMapEntries);
    }

    private void listMissing() throws IOException {

        List<String> banks = MapHelper.parseBanks(this.bankDirectory);
        Map<Long, String> checksums = MapHelper.computeChecksums(banks);

        System.out.println("- Bank parsing done: " + this.bankDirectory);
        System.out.println("  -> File count: " + banks.size());
        System.out.println("  -> Files: " + banks);
        System.out.println("  -> Checksums: " + checksums);

        BankMap map = loadBankMap();
        Map<Long, String> newChecksums = MapHelper.findNewChecksums(map, checksums);

        System.out.println("  -> Absent from Bnk1.map: " + newChecksums);
    }

    private void fixMissing() throws IOException {

        List<String> banks = MapHelper.parseBanks(this.bankDirectory);
        Map<Long, String> checksums = MapHelper.computeChecksums(banks);
        BankMap map = loadBankMap();

        MapHelper.findNewChecksums(map, checksums)

                .keySet()

                .forEach(map::addMagicEntry);

        saveBankMap(map);

        System.out.println("Bnk1.map fixing done: " + this.mapFile);
    }

    private BankMap loadBankMap() throws IOException {
        Path mapFilePath = Paths.get(this.mapFile);

        byte[] mapContents = Files.readAllBytes(mapFilePath);
        ByteArrayInputStream mapInputStream = new ByteArrayInputStream(mapContents);
        return MapParser.load(mapInputStream).parse();
    }

    private void saveBankMap(BankMap map) throws IOException {
        Path mapFilePath = Paths.get(this.mapFile);

        ByteArrayOutputStream outputStream = MapWriter.load(map).write();
        Files.write(mapFilePath, outputStream.toByteArray());
    }
}