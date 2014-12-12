package fr.tduf.cli.tools;

import fr.tduf.libunlimited.low.files.db.DatabaseReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Command line interface for handling TDU database.
 */
public class DatabaseTool {

    @Argument
    private List<String> arguments = new ArrayList<>();

    /**
     * Utility entry point - till a CLI comes
     */
    public static void main(String[] args) throws IOException {
        new DatabaseTool().doMain(args);
    }

    private void doMain(String[] args) throws FileNotFoundException {
        if (!checkArguments(args)) {
            return;
        }
        String databaseFolderName = arguments.get(0);
        String outputSubDirectorySuffix = "tdu-database-dump";

        File outputDirectory = new File(outputSubDirectorySuffix);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();

        }

        for(DbDto.Topic currentTopic : DbDto.Topic.values()) {
            DbDto dbDto = DatabaseReadWriteHelper.readDatabase(currentTopic, databaseFolderName);

            DatabaseReadWriteHelper.writeDatabaseToJson(dbDto, outputDirectory.getAbsolutePath());

            System.out.println("Writing done for topic: " + currentTopic);
            System.out.println("Location: " + outputDirectory + File.separator + currentTopic.getLabel() + ".json");
        }
    }

    private boolean checkArguments(String[] args) {
        //TODO use same as Mapping Tool
        try {
            CmdLineParser parser = new CmdLineParser(this);
            parser.parseArgument(args);

            if( arguments.isEmpty() ) {
                throw new CmdLineException(parser, "Error: No argument is given", null);
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java DatabaseReadWriteHelper <DATABASE FOLDER>");
            System.err.println("  Example: java DatabaseReadWriteHelper  \"D:\\Jeux\\Test Drive Unlimited\\Euro\\NoBnk\\DataBase\"");
            return false;
        }
        return true;
    }
}