package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityChecker;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityFixer;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fr.tduf.cli.tools.DatabaseTool.Command.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Command line interface for handling TDU database.
 */
public class DatabaseTool extends GenericTool {

    @Option(name = "-d", aliases = "--databaseDir", usage = "UNPACKED TDU database directory, defaults to current directory.")
    private String databaseDirectory;

    @Option(name = "-j", aliases = "--jsonDir", usage = "Source (gen) of target (dump) directory for JSON files, defaults to current directory\\tdu-database-dump.")
    private String jsonDirectory;

    @Option(name = "-o", aliases = "--outputDatabaseDir", usage = "Fixed TDU database directory, defaults to current directory\\tdu-database-fixed.")
    private String outputDatabaseDirectory;

    @Option(name = "-c", aliases = "--clear", usage = "Not mandatory. Indicates unpacked TDU files do not need to be unencrypted and encrypted back.")
    private boolean withClearContents = false;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        CHECK("check", "Tries to load database and display integrity errors, if any."),
        DUMP("dump", "Writes full database contents to JSON files."),
        GEN("gen", "Writes TDU database files from JSON files."),
        FIX("fix", "Loads database, checks for integrity errors and create database copy with fixed ones.");

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
    protected boolean commandDispatch() throws Exception {
        switch (command) {
            case DUMP:
                dump();
                break;
            case CHECK:
                check();
                break;
            case GEN:
                gen();
                break;
            case FIX:
                fix();
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
            jsonDirectory = "tdu-database-dump";
        }

        if (outputDatabaseDirectory == null) {
            outputDatabaseDirectory = "tdu-database-fixed";
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
                CHECK.label + " -d \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\"",
                FIX.label + " -c -d \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\" -o \"C:\\Users\\Bill\\Desktop\\tdu-database-fixed\""
        );
    }

    private void dump() throws IOException {
        createDirectoryIfNotExists(this.jsonDirectory);

        System.out.println("-> Source directory: " + databaseDirectory);
        System.out.println("Dumping TDU database to JSON, please wait...");
        System.out.println();

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
            System.out.println("-> " + writtenFileName);
            System.out.println();
        }

        System.out.println("All done!");
    }

    private void gen() throws IOException {
        createDirectoryIfNotExists(this.databaseDirectory);

        System.out.println("-> Source directory: " + this.jsonDirectory);
        System.out.println("Generating TDU database from JSON, please wait...");
        System.out.println();

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            System.out.println("-> Now processing topic: " + currentTopic + "...");

            DbDto dbDto = DatabaseReadWriteHelper.readDatabaseFromJson(currentTopic, this.jsonDirectory);

            if (dbDto == null) {
                System.out.println("  !Database contents not found for topic " + currentTopic + ", skipping...");
                System.out.println();
                continue;
            }

            writeDatabaseTopic(dbDto, this.databaseDirectory);
        }

        System.out.println("All done!");
    }

    private void check() throws Exception {
        List<IntegrityError> integrityErrors = new ArrayList<>();
        checkAndReturnIntegrityErrorsAndObjects(integrityErrors);

        if(!integrityErrors.isEmpty()) {
            throw new IllegalArgumentException("At least one integrity error has been found, your database is not ready-to-use.");
        }

        System.out.println("All done.");
    }

    private void fix() throws IOException {
        List<IntegrityError> integrityErrors = new ArrayList<>();
        List<DbDto> databaseObjects = checkAndReturnIntegrityErrorsAndObjects(integrityErrors);

        if(integrityErrors.isEmpty()) {
            System.out.println("No error detected - a fix is not necessary.");
            return;
        }

        System.out.println("-> Now fixing database...");
        DatabaseIntegrityFixer databaseIntegrityFixer = DatabaseIntegrityFixer.load(databaseObjects, integrityErrors);
        List<IntegrityError> remainingIntegrityErrors = databaseIntegrityFixer.fixAllContentsObjects();
        List<DbDto> fixedDatabaseObjects = databaseIntegrityFixer.getDbDtos();

        printIntegrityErrors(remainingIntegrityErrors);

        if(fixedDatabaseObjects.isEmpty()) {
            System.out.println("ERROR! Unrecoverable integrity errors spotted. Consider restoring TDU database from backup.");
        } else {
            System.out.println("-> Now writing database to " + this.outputDatabaseDirectory + "...");
            System.out.println();

            createDirectoryIfNotExists(this.outputDatabaseDirectory);

            for (DbDto databaseObject : fixedDatabaseObjects) {
                System.out.println("-> Now processing topic: " + databaseObject.getStructure().getTopic() + "...");

                writeDatabaseTopic(databaseObject, this.outputDatabaseDirectory);
            }

            if (!remainingIntegrityErrors.isEmpty()) {
                System.out.println("WARNING! TDU database has been rewritten, but some integrity errors do remain. Your game may not work as expected.");
            }
        }

        System.out.println("All done.");
    }

    private List<DbDto> checkAndReturnIntegrityErrorsAndObjects(List<IntegrityError> integrityErrors) throws IOException {
        System.out.println("-> Source directory: " + databaseDirectory);
        System.out.println("Checking TDU database, please wait...");
        System.out.println();

        System.out.println("-> Now loading database, step 1...");

        List<DbDto> dbDtos = loadAndCheckDatabase(integrityErrors);

        System.out.println("-> step 1 finished.");
        System.out.println();

        if (dbDtos.size() == DbDto.Topic.values().length) {
            System.out.println("-> Now checking integrity between topics, step 2...");

            betweenTopicsCheck(dbDtos, integrityErrors);

            System.out.println("-> step 2 finished.");
            System.out.println();
        }

        printIntegrityErrors(integrityErrors);

        return dbDtos;
    }

    private List<DbDto> loadAndCheckDatabase(List<IntegrityError> integrityErrors) throws IOException {
        requireNonNull(integrityErrors, "A list is required");

        List<DbDto> allDtos = new ArrayList<>();

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            System.out.println("  -> Now processing topic: " + currentTopic + "...");

            int initialErrorCount = integrityErrors.size();

            DbDto dbDto = DatabaseReadWriteHelper.readDatabase(currentTopic, this.databaseDirectory, this.withClearContents, integrityErrors);

            if (dbDto == null) {
                System.out.println("  (!)Database contents not found for topic " + currentTopic + ", skipping.");
                System.out.println();
                continue;
            }

            System.out.println("  .Found topic: " + currentTopic + ", " + (integrityErrors.size() - initialErrorCount) + " error(s).");
            System.out.println("  .Content line count: " + dbDto.getData().getEntries().size());

            if (!dbDto.getResources().isEmpty()) {
                System.out.println("  .Resource count per locale: ");
                dbDto.getResources().stream()

                        .sorted(
                                (dbResourceDto1, dbResourceDto2) -> dbResourceDto1.getLocale().name().compareTo(dbResourceDto2.getLocale().name()))

                        .forEach(
                                (dbResourceDto) -> System.out.println("    >" + dbResourceDto.getLocale() + "=" + dbResourceDto.getEntries().size()));
            }

            System.out.println();

            if (integrityErrors.isEmpty()) {
                allDtos.add(dbDto);
            }
        }

        return allDtos;
    }

    private void writeDatabaseTopic(DbDto dbDto, String outputDatabaseDirectory) throws IOException {
        List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabase(dbDto, outputDatabaseDirectory, this.withClearContents);

        System.out.println("Writing done for topic: " + dbDto.getStructure().getTopic());
        writtenFiles.stream()
                .forEach((fileName) -> System.out.println("-> " + fileName));
        System.out.println();
    }

    private static void betweenTopicsCheck(List<DbDto> allDtos, List<IntegrityError> integrityErrors) {
        requireNonNull(integrityErrors, "A list is required");

        integrityErrors.addAll(DatabaseIntegrityChecker.load(allDtos).checkAllContentsObjects());
    }

    private static void printIntegrityErrors(List<IntegrityError> integrityErrors) {
        if(!integrityErrors.isEmpty()) {
            System.out.println("-> Integrity errors (" + integrityErrors.size() + "):");

            integrityErrors.forEach(
                    (integrityError) -> {
                        String errorMessage = String.format(integrityError.getErrorMessageFormat(), integrityError.getInformation());
                        System.out.println("  (!)" + errorMessage);
                    });
        }
    }

    // TODO replace with helper
    private static void createDirectoryIfNotExists(String directoryToCreate) {
        File outputDirectory = new File(directoryToCreate);
        if (!outputDirectory.exists()) {
            boolean isCreated = outputDirectory.mkdirs();
            assert isCreated;
        }
    }
}