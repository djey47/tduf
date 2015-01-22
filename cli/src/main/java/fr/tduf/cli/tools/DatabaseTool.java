package fr.tduf.cli.tools;

import fr.tduf.cli.common.CommandHelper;
import fr.tduf.libunlimited.low.files.db.DatabaseReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.cli.tools.DatabaseTool.Command.CHECK;
import static fr.tduf.cli.tools.DatabaseTool.Command.DUMP;
import static java.util.Arrays.asList;

/**
 * Command line interface for handling TDU database.
 */
public class DatabaseTool extends GenericTool {

    @Option(name = "-d", aliases = "--databaseDir", usage = "UNPACKED TDU database directory, defaults to current directory.")
    private String databaseDirectory;

    @Option(name = "-o", aliases = "--outputDir", usage = "Target directory for generated JSON files, defaults to current directory\\tdu-database-dump.")
    private String outputDirectory;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        CHECK("check", "Tries to load database and display integrity errors, if any."),
        DUMP("dump", "Writes full database contents to JSON files.");

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
        new DatabaseTool().doMain(args);
    }

    @Override
    protected boolean commandDispatch() throws IOException {
        switch (command) {
            case DUMP:
                dump();
                break;
            case CHECK:
                databaseCheck();
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
    protected void checkAndAssignDefaultParameters(CmdLineParser parser) {
        // Database directory: defaulted to current
        if (databaseDirectory == null) {
            databaseDirectory = ".";
        }

        // Map file: defaulted to current directory\tdu-database-dump
        if (outputDirectory == null) {
            outputDirectory = "." + File.separator + "tdu-database-dump";
        }
    }

    @Override
    protected CommandHelper.CommandEnum getCommand() {
        return Command.DUMP;
    }

    @Override
    protected List<String> getExamples() {
        return asList(
                DUMP.label + " --databaseDir \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\"",
                CHECK.label + " -d \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\""
        );
    }

    private void dump() throws FileNotFoundException {
        File outputDirectory = new File(this.outputDirectory);
        if (!outputDirectory.exists()) {
            assert outputDirectory.mkdirs();
        }

        System.out.println("-> Source: " + databaseDirectory);
        System.out.println("Dumping TDU database to JSON, please wait...");

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            System.out.println("-> Now processing topic: " + currentTopic + "...");

            DbDto dbDto = DatabaseReadWriteHelper.readDatabase(currentTopic, this.databaseDirectory, new ArrayList<>());

            DatabaseReadWriteHelper.writeDatabaseToJson(dbDto, outputDirectory.toString());

            System.out.println("Writing done for topic: " + currentTopic);
            System.out.println("Location: " + outputDirectory + File.separator + currentTopic.getLabel() + ".json");
            System.out.println();
        }

        System.out.println("All done!");
    }

    private void databaseCheck() throws FileNotFoundException {

        System.out.println("Checking TDU database, please wait...");
        System.out.println("-> Source directory: " + databaseDirectory);

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            System.out.println("-> Now processing topic: " + currentTopic + "...");

            List<IntegrityError> integrityErrors = new ArrayList<>();
            DbDto dbDto = DatabaseReadWriteHelper.readDatabase(currentTopic, this.databaseDirectory, integrityErrors);

            if (dbDto == null) {
                System.out.println("  !Database contents not found for topic " + currentTopic + ", skipping...");
                continue;
            }

            System.out.println("  .Content line count: " + dbDto.getData().getEntries().size());
            System.out.println("  .Found topic: " + currentTopic + ", " + integrityErrors.size() + " error(s).");
            if(!integrityErrors.isEmpty()) {
                integrityErrors.forEach(
                        (integrityError) -> {
                            String errorMessage = String.format(integrityError.getErrorMessageFormat(), integrityError.getInformation());
                            System.out.println("    !" + errorMessage);
                        });
            }

            if (!dbDto.getResources().isEmpty()) {
                System.out.println("  .Resource count per locale: ");
                dbDto.getResources().forEach(
                        (dbResourceDto) -> System.out.println("    ." + dbResourceDto.getLocale() + "=" + dbResourceDto.getEntries().size()));
            }
        }

        System.out.println("All done!");
    }
}