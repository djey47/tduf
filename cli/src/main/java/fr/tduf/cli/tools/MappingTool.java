package fr.tduf.cli.tools;

import fr.tduf.libunlimited.low.files.banks.mapping.MapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.parser.MapParser;
import fr.tduf.libunlimited.low.files.banks.mapping.writer.MapWriter;
import org.kohsuke.args4j.Argument;
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
import java.util.*;

import static java.lang.Long.compare;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Command line interface for handling TDU file mapping.
 */
public class MappingTool {

    @Option(name="-b", aliases = "--bnkDir", usage = "TDU banks directory (Bnk), defaults to current directory." )
    private String bankDirectory;

    @Option(name="-m", aliases = "--mapFile", usage = "Bnk1.map file, defaults to TDU banks directory\\Bnk1.map." )
    private String mapFile;

    @Argument
    private List<String> arguments = new ArrayList<>();

    private Command command;

    /**
     * All available commands
     */
    enum Command {
        INFO("info"),
        LIST("list"),
        LIST_MISSING("list-missing"),
        FIX_MISSING("fix-missing");

        final String label;

        Command(String label) {
            this.label = label;
        }

        private static Set<String> labels() {
            return asList(values()).stream()

                    .map(cmd -> cmd.label)

                    .collect(toSet());
        }

        private static Command fromLabel(String label) {
            return asList(values()).stream()

                    .filter(cmd -> cmd.label.equals(label))

                    .findAny()

                    .get();
        }
    }

    /**
     * Utility entry point
     */
    public static void main(String[] args) throws IOException {
        new MappingTool().doMain(args);
    }

    boolean checkArgumentsAndOptions(String[] args) {
        try {
            CmdLineParser parser = new CmdLineParser(this);
            parser.parseArgument(args);

            checkCommand(parser);

            // Bnk directory: defaulted to current
            if (bankDirectory == null) {
                bankDirectory = ".";
            }

            // Map file: defaulted to current directory\Bnk1.map
            if (mapFile == null) {
                mapFile = bankDirectory + File.separator + "Bnk1.map";
            }
        } catch (CmdLineException e) {
            String displayedName = this.getClass().getCanonicalName();

            System.err.println(e.getMessage());
            System.err.println("Syntax: " + displayedName +  " command [-options]");
            System.err.println("  Commands:");

            Command.labels().stream()

                    .forEach(System.err::println);

            System.err.println("  Options:");
            e.getParser().printUsage(System.err);
            System.err.println("  Example:");
            System.err.println(displayedName + " " + Command.INFO.label + " --bnkDir \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\"");
            return false;
        }
        return true;
    }

    private void doMain(String[] args) throws IOException {
        if (!checkArgumentsAndOptions(args)) {
            System.exit(1);
        }

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
                System.err.println("Error: command is not implemented, yet.");
                System.exit(1);
                break;
        }
    }

    private void checkCommand(CmdLineParser parser) throws CmdLineException {

        if( arguments.isEmpty() ) {
            throw new CmdLineException(parser, "Error: No command is given.", null);
        }

        String commandArgument = arguments.get(0);

        if ( !Command.labels().contains(commandArgument) ) {
            throw new CmdLineException(parser, "Error: An unsupported command is given.", null);
        }

        this.command = Command.fromLabel(commandArgument);
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

    String getBankDirectory() {
        return bankDirectory;
    }

    String getMapFile() {
        return mapFile;
    }

    Command getCommand() {
        return command;
    }
}