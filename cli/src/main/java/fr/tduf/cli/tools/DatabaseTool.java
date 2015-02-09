package fr.tduf.cli.tools;

import fr.tduf.cli.common.CommandHelper;
import fr.tduf.libunlimited.high.files.db.DatabaseIntegrityChecker;
import fr.tduf.libunlimited.low.files.db.rw.DatabaseReadWriteHelper;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.cli.tools.DatabaseTool.Command.*;
import static java.util.Arrays.asList;

/**
 * Command line interface for handling TDU database.
 */
public class DatabaseTool extends GenericTool {

    @Option(name = "-d", aliases = "--databaseDir", usage = "UNPACKED TDU database directory, defaults to current directory.")
    private String databaseDirectory;

    @Option(name = "-j", aliases = "--jsonDir", usage = "Source (gen) of target (dump) directory for JSON files, defaults to current directory\\tdu-database-dump.")
    private String jsonDirectory;

    @Option(name = "-c", aliases = "--clear", usage = "Not mandatory. Indicates unpacked TDU files do not need to be unencrypted and encrypted back.")
    private boolean withClearContents;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        CHECK("check", "Tries to load database and display integrity errors, if any."),
        DUMP("dump", "Writes full database contents to JSON files."),
        GEN("gen", "Writes TDU database files from JSON files.");

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
            case GEN:
                gen();
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
        if (databaseDirectory == null) {
            databaseDirectory = ".";
        }

        if (jsonDirectory == null) {
            jsonDirectory = "." + File.separator + "tdu-database-dump";
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
                GEN.label + " --jsonDir \"C:\\Users\\Bill\\Desktop\\json-database\" --databaseDir \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\"",
                CHECK.label + " -d \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\""
        );
    }

    private void dump() throws IOException {
        createDirectoryIfNotExists(this.jsonDirectory);

        System.out.println("-> Source: " + databaseDirectory);
        System.out.println("Dumping TDU database to JSON, please wait...");

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            System.out.println("-> Now processing topic: " + currentTopic + "...");

            DbDto dbDto = DatabaseReadWriteHelper.readDatabase(currentTopic, this.databaseDirectory, this.withClearContents, new ArrayList<>());

            if (dbDto == null) {
                System.out.println("  !Database contents not found for topic " + currentTopic + ", skipping...");
                System.out.println();
                continue;
            }

            String writtenFileName = DatabaseReadWriteHelper.writeDatabaseToJson(dbDto, this.jsonDirectory);

            System.out.println("Writing done for topic: " + currentTopic);
            System.out.println("-> Location: " + writtenFileName);
            System.out.println();
        }

        System.out.println("All done!");
    }

    private void gen() throws IOException {
        createDirectoryIfNotExists(this.databaseDirectory);

        System.out.println("-> Source: " + this.jsonDirectory);
        System.out.println("Generating TDU database from JSON, please wait...");

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            System.out.println("-> Now processing topic: " + currentTopic + "...");

            DbDto dbDto = DatabaseReadWriteHelper.readDatabaseFromJson(currentTopic, this.jsonDirectory);

            if (dbDto == null) {
                System.out.println("  !Database contents not found for topic " + currentTopic + ", skipping...");
                System.out.println();
                continue;
            }

            List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabase(dbDto, this.databaseDirectory, this.withClearContents);

            System.out.println("Writing done for topic: " + currentTopic);
            writtenFiles.stream()
                    .forEach((fileName) -> System.out.println("-> " + fileName));
            System.out.println();
        }

        System.out.println("All done!");
    }

    // TODO simplify and reuse integrity error list + Change output info (step 1, step 2 ...)
    private void databaseCheck() throws IOException {

        System.out.println("Checking TDU database, please wait...");
        System.out.println("-> Source directory: " + databaseDirectory);

        List<DbDto> dbDtos = perTopicCheck();

        if (!dbDtos.isEmpty()) {
            betweenTopicsCheck(dbDtos);
        }

        System.out.println("All done!");
    }

    private List<DbDto> perTopicCheck() throws IOException {
        List<DbDto> allDtos = new ArrayList<>();

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            System.out.println("-> Now processing topic: " + currentTopic + "...");

            List<IntegrityError> integrityErrors = new ArrayList<>();
            DbDto dbDto = DatabaseReadWriteHelper.readDatabase(currentTopic, this.databaseDirectory, this.withClearContents, integrityErrors);

            if (dbDto == null) {
                System.out.println("  !Database contents not found for topic " + currentTopic + ", skipping...");
                continue;
            }

            System.out.println("  .Content line count: " + dbDto.getData().getEntries().size());
            System.out.println("  .Found topic: " + currentTopic + ", " + integrityErrors.size() + " error(s).");
            printIntegrityErrors(integrityErrors);

            if (!dbDto.getResources().isEmpty()) {
                System.out.println("  .Resource count per locale: ");
                dbDto.getResources().forEach(
                        (dbResourceDto) -> System.out.println("    ." + dbResourceDto.getLocale() + "=" + dbResourceDto.getEntries().size()));
            }

            if (integrityErrors.isEmpty()) {
                allDtos.add(dbDto);
            }
        }

        return allDtos;
    }

    private static void betweenTopicsCheck(List<DbDto> allDtos) {
        System.out.println("-> Now checking integrity between topics...");

        List<IntegrityError> integrityErrors = DatabaseIntegrityChecker.load(allDtos).checkAll();

        printIntegrityErrors(integrityErrors);
    }

    private static void printIntegrityErrors(List<IntegrityError> integrityErrors) {
        if(!integrityErrors.isEmpty()) {
            integrityErrors.forEach(
                    (integrityError) -> {
                        String errorMessage = String.format(integrityError.getErrorMessageFormat(), integrityError.getInformation());
                        System.out.println("    !" + errorMessage);
                    });
        }
    }

    private static void createDirectoryIfNotExists(String directoryToCreate) {
        File outputDirectory = new File(directoryToCreate);
        if (!outputDirectory.exists()) {
            boolean isCreated = outputDirectory.mkdirs();
            assert isCreated;
        }
    }
}