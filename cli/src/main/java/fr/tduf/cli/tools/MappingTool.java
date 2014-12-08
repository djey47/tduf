package fr.tduf.cli.tools;

import fr.tduf.libunlimited.low.files.banks.mapping.MapHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.parser.MapParser;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Command line interface for handling TDU file mapping.
 */
public class MappingTool {

    @Argument
    private List<String> arguments = new ArrayList<>();

    /**
     * Utility entry point - till a CLI comes
     */
    public static void main(String[] args) throws IOException {
        new MappingTool().doMain(args);
    }

    private void doMain(String[] args) throws IOException {
        if (!checkArguments(args)) {
            return;
        }
        String bnkFolderName = arguments.get(0);

        System.out.println("BNK root folder: " + bnkFolderName);

        List<String> banks = MapHelper.parseBanks(bnkFolderName);

        System.out.println("Bank parsing done.");
        System.out.println("File count: " + banks.size());
        System.out.println("Files: " + banks);

        Map<Long, String> checksums = MapHelper.computeChecksums(banks);

        System.out.println("Checksums: " + checksums);

        String mapFileName = bnkFolderName + File.separator + "Bnk1.map";
        byte[] mapContents = Files.readAllBytes(Paths.get(mapFileName));
        ByteArrayInputStream mapInputStream = new ByteArrayInputStream(mapContents);
        BankMap map = MapParser.load(mapInputStream).parse();

        System.out.println("Bnk1.map parsing done: " + mapFileName);
        System.out.println("Entry count: " + map.getEntries().size());

        Map<Long, String> newChecksums = MapHelper.findNewChecksums(map, checksums);

        System.out.println("Contents which are absent from Bnk1.map: " + newChecksums);
    }

    private boolean checkArguments(String[] args) {
        try {
            CmdLineParser parser = new CmdLineParser(this);
            parser.parseArgument(args);

            if( arguments.isEmpty() ) {
                throw new CmdLineException(parser, "Error: No argument is given", null);
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java MapHelper <BNK FOLDER>");
            System.err.println("  Example: java MapHelper \"D:\\Jeux\\Test Drive Unlimited\\Euro\\Bnk\"");
            return false;
        }
        return true;
    }
}