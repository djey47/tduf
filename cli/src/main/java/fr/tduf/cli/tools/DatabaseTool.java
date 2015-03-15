package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityChecker;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityFixer;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
        FilesHelper.createDirectoryIfNotExists(this.jsonDirectory);

        outLine("-> Source directory: " + databaseDirectory);
        outLine("Dumping TDU database to JSON, please wait...");
        outLine();

        List<String> writtenFileNames = new ArrayList<>();
        List<DbDto.Topic> missingTopicContents = new ArrayList<>();
        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            outLine("-> Now processing topic: " + currentTopic + "...");

            DbDto dbDto = DatabaseReadWriteHelper.readDatabase(currentTopic, this.databaseDirectory, this.withClearContents, new ArrayList<>());

            if (dbDto == null) {
                outLine("  !Database contents not found for topic " + currentTopic + ", skipping...");
                outLine();

                missingTopicContents.add(currentTopic);

                continue;
            }

            String writtenFileName = DatabaseReadWriteHelper.writeDatabaseToJson(dbDto, this.jsonDirectory);

            outLine("Writing done for topic: " + currentTopic);
            outLine("-> " + writtenFileName);
            outLine();

            writtenFileNames.add(writtenFileName);
        }

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("missingTopicContents", missingTopicContents);
        resultInfo.put("writtenFiles", writtenFileNames);
        commandResult = resultInfo;
    }

    private void gen() throws IOException {
        FilesHelper.createDirectoryIfNotExists(this.databaseDirectory);

        outLine("-> Source directory: " + this.jsonDirectory);
        outLine("Generating TDU database from JSON, please wait...");
        outLine();

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            outLine("-> Now processing topic: " + currentTopic + "...");

            DbDto dbDto = DatabaseReadWriteHelper.readDatabaseFromJson(currentTopic, this.jsonDirectory);

            if (dbDto == null) {
                outLine("  !Database contents not found for topic " + currentTopic + ", skipping...");
                outLine();
                continue;
            }

            writeDatabaseTopic(dbDto, this.databaseDirectory);
        }

        outLine("All done!");
    }

    private void check() throws Exception {
        List<IntegrityError> integrityErrors = new ArrayList<>();
        checkAndReturnIntegrityErrorsAndObjects(integrityErrors);

        if(!integrityErrors.isEmpty()) {
            throw new IllegalArgumentException("At least one integrity error has been found, your database is not ready-to-use.");
        }

        outLine("All done.");
    }

    private void fix() throws IOException {
        List<IntegrityError> integrityErrors = new ArrayList<>();
        List<DbDto> databaseObjects = checkAndReturnIntegrityErrorsAndObjects(integrityErrors);

        if(integrityErrors.isEmpty()) {
            outLine("No error detected - a fix is not necessary.");
            return;
        }

        outLine("-> Now fixing database...");
        DatabaseIntegrityFixer databaseIntegrityFixer = DatabaseIntegrityFixer.load(databaseObjects, integrityErrors);
        List<IntegrityError> remainingIntegrityErrors = databaseIntegrityFixer.fixAllContentsObjects();
        List<DbDto> fixedDatabaseObjects = databaseIntegrityFixer.getDbDtos();

        printIntegrityErrors(remainingIntegrityErrors);

        if(fixedDatabaseObjects.isEmpty()) {
            outLine("ERROR! Unrecoverable integrity errors spotted. Consider restoring TDU database from backup.");
        } else {
            outLine("-> Now writing database to " + this.outputDatabaseDirectory + "...");
            outLine();

            FilesHelper.createDirectoryIfNotExists(this.outputDatabaseDirectory);

            for (DbDto databaseObject : fixedDatabaseObjects) {
                outLine("-> Now processing topic: " + databaseObject.getStructure().getTopic() + "...");

                writeDatabaseTopic(databaseObject, this.outputDatabaseDirectory);
            }

            if (!remainingIntegrityErrors.isEmpty()) {
                outLine("WARNING! TDU database has been rewritten, but some integrity errors do remain. Your game may not work as expected.");
            }
        }

        outLine("All done.");
    }

    private List<DbDto> checkAndReturnIntegrityErrorsAndObjects(List<IntegrityError> integrityErrors) throws IOException {
        outLine("-> Source directory: " + databaseDirectory);
        outLine("Checking TDU database, please wait...");
        outLine();

        outLine("-> Now loading database, step 1...");

        List<DbDto> dbDtos = loadAndCheckDatabase(integrityErrors);

        outLine("-> step 1 finished.");
        outLine();

        if (dbDtos.size() == DbDto.Topic.values().length) {
            outLine("-> Now checking integrity between topics, step 2...");

            betweenTopicsCheck(dbDtos, integrityErrors);

            outLine("-> step 2 finished.");
            outLine();
        }

        printIntegrityErrors(integrityErrors);

        return dbDtos;
    }

    private List<DbDto> loadAndCheckDatabase(List<IntegrityError> integrityErrors) throws IOException {
        requireNonNull(integrityErrors, "A list is required");

        List<DbDto> allDtos = new ArrayList<>();

        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            outLine("  -> Now processing topic: " + currentTopic + "...");

            int initialErrorCount = integrityErrors.size();

            DbDto dbDto = DatabaseReadWriteHelper.readDatabase(currentTopic, this.databaseDirectory, this.withClearContents, integrityErrors);

            if (dbDto == null) {
                outLine("  (!)Database contents not found for topic " + currentTopic + ", skipping.");
                outLine();
                continue;
            }

            outLine("  .Found topic: " + currentTopic + ", " + (integrityErrors.size() - initialErrorCount) + " error(s).");
            outLine("  .Content line count: " + dbDto.getData().getEntries().size());

            if (!dbDto.getResources().isEmpty()) {
                outLine("  .Resource count per locale: ");
                dbDto.getResources().stream()

                        .sorted(
                                (dbResourceDto1, dbResourceDto2) -> dbResourceDto1.getLocale().name().compareTo(dbResourceDto2.getLocale().name()))

                        .forEach(
                                (dbResourceDto) -> outLine("    >" + dbResourceDto.getLocale() + "=" + dbResourceDto.getEntries().size()));
            }

            outLine();

            if (integrityErrors.isEmpty()) {
                allDtos.add(dbDto);
            }
        }

        return allDtos;
    }

    private void writeDatabaseTopic(DbDto dbDto, String outputDatabaseDirectory) throws IOException {
        List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabase(dbDto, outputDatabaseDirectory, this.withClearContents);

        outLine("Writing done for topic: " + dbDto.getStructure().getTopic());
        writtenFiles.stream()
                .forEach((fileName) -> outLine("-> " + fileName));
        outLine();
    }

    private static void betweenTopicsCheck(List<DbDto> allDtos, List<IntegrityError> integrityErrors) {
        requireNonNull(integrityErrors, "A list is required");

        integrityErrors.addAll(DatabaseIntegrityChecker.load(allDtos).checkAllContentsObjects());
    }

    private void printIntegrityErrors(List<IntegrityError> integrityErrors) {
        if(!integrityErrors.isEmpty()) {
            outLine("-> Integrity errors (" + integrityErrors.size() + "):");

            integrityErrors.forEach(
                    (integrityError) -> {
                        String errorMessage = String.format(integrityError.getErrorMessageFormat(), integrityError.getInformation());
                        outLine("  (!)" + errorMessage);
                    });
        }
    }
}