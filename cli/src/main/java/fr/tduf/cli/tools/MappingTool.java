package fr.tduf.cli.tools;

import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.parser.MapParser;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
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
            case LIST_MISSING:
                listMissing();
                break;
//            case FIX_MISSING:
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

        byte[] mapContents = Files.readAllBytes(Paths.get(this.mapFile));
        ByteArrayInputStream mapInputStream = new ByteArrayInputStream(mapContents);
        BankMap map = MapParser.load(mapInputStream).parse();
        Collection<BankMap.Entry> mapEntries = map.getEntries();

        System.out.println("- Bnk1.map parsing done: " + this.mapFile);
        System.out.println("  -> Entry count: " + mapEntries.size());
        System.out.println("  -> Entries: " + mapEntries);
    }

    private void listMissing() {


//        List<String> banks = MapHelper.parseBanks(bnkFolderName);
//
//        System.out.println("Bank parsing done.");
//        System.out.println("File count: " + banks.size());
//        System.out.println("Files: " + banks);
//
//        Map<Long, String> checksums = MapHelper.computeChecksums(banks);
//
//        System.out.println("Checksums: " + checksums);
//
//
//
//        Map<Long, String> newChecksums = MapHelper.findNewChecksums(map, checksums);
//
//        System.out.println("Contents which are absent from Bnk1.map: " + newChecksums);

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