package fr.tduf.cli.tools;

import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.cli.tools.dto.DatabaseIntegrityErrorDto;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityChecker;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityFixer;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static fr.tduf.cli.tools.DatabaseTool.Command.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Command line interface for handling TDU database.
 */
public class DatabaseTool extends GenericTool {

    @Option(name = "-d", aliases = "--databaseDir", usage = "UNPACKED TDU database directory, defaults to current directory.")
    private String databaseDirectory;

    @Option(name = "-j", aliases = "--jsonDir", usage = "Source (gen/apply-patch) or target (dump) directory for JSON files, defaults to current directory\\tdu-database-dump.")
    private String jsonDirectory;

    @Option(name = "-o", aliases = "--outputDatabaseDir", usage = "Fixed/Patched TDU database directory, defaults to current directory\\tdu-database-fixed or \\tdu-database-patched.")
    private String outputDatabaseDirectory;

    @Option(name = "-p", aliases = "--patchFile", usage = "File describing mini patch to apply (required for apply-patch operation).")
    private String patchFile;

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
        FIX("fix", "Loads database, checks for integrity errors and create database copy with fixed ones."),
        APPLY_PATCH("apply-patch", "Modifies database contents and resources as described in a JSON mini patch file.");

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
                return true;
            case CHECK:
                check();
                return true;
            case GEN:
                gen();
                return true;
            case FIX:
                fix();
                return true;
            case APPLY_PATCH:
                applyPatch();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void assignCommand(String commandArgument) {
        this.command = (Command) CommandHelper.fromLabel(getCommand(), commandArgument);
    }

    @Override
    protected void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException {
        if (databaseDirectory == null) {
            databaseDirectory = ".";
        }

        if (jsonDirectory == null) {
            if (DUMP == command) {
                jsonDirectory = "tdu-database-dump";
            } else if (APPLY_PATCH == command) {
                throw new CmdLineException(parser, "Error: jsonDirectory is required as source database.", null);
            }
        }

        if (outputDatabaseDirectory == null) {
            if (DUMP == command) {
                outputDatabaseDirectory = "tdu-database-fixed";
            } else if (APPLY_PATCH == command) {
                outputDatabaseDirectory = "tdu-database-patched";
            }
        }

        if (patchFile == null && APPLY_PATCH == command) {
            throw new CmdLineException(parser, "Error: patchFile is required.", null);
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
                FIX.label + " -c -d \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\" -o \"C:\\Users\\Bill\\Desktop\\tdu-database-fixed\"",
                APPLY_PATCH.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\miniPatch.json\""
        );
    }

    private void applyPatch() {
//        FilesHelper.createDirectoryIfNotExists(this.outputDatabaseDirectory);
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

        List<DbDto.Topic> missingTopicContents = new ArrayList<>();
        List<String> writtenFileNames = new ArrayList<>();
        for (DbDto.Topic currentTopic : DbDto.Topic.values()) {
            outLine("-> Now processing topic: " + currentTopic + "...");

            DbDto dbDto = DatabaseReadWriteHelper.readDatabaseFromJson(currentTopic, this.jsonDirectory);

            if (dbDto == null) {
                outLine("  !Database contents not found for topic " + currentTopic + ", skipping...");
                outLine();

                missingTopicContents.add(currentTopic);

                continue;
            }

            writtenFileNames.addAll(writeDatabaseTopic(dbDto, this.databaseDirectory));
        }

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("missingJsonTopicContents", missingTopicContents);
        resultInfo.put("writtenFiles", writtenFileNames);
        commandResult = resultInfo;
    }

    private void check() throws Exception {
        List<IntegrityError> integrityErrors = new ArrayList<>();
        checkAndReturnIntegrityErrorsAndObjects(integrityErrors);

        if(!integrityErrors.isEmpty()) {
            outLine("At least one integrity error has been found, your database is not ready-to-use.");
        }

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("integrityErrors", toDatabaseIntegrityErrors(integrityErrors));

        commandResult = resultInfo;
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

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("integrityErrorsRemaining", toDatabaseIntegrityErrors(remainingIntegrityErrors));

        resultInfo.put("fixedDatabaseLocation", this.outputDatabaseDirectory);
        commandResult = resultInfo;
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

    private List<String> writeDatabaseTopic(DbDto dbDto, String outputDatabaseDirectory) throws IOException {
        List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabase(dbDto, outputDatabaseDirectory, this.withClearContents);

        outLine("Writing done for topic: " + dbDto.getStructure().getTopic());
        writtenFiles.stream()
                .forEach((fileName) -> outLine("-> " + fileName));
        outLine();

        return writtenFiles;
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

    private static void betweenTopicsCheck(List<DbDto> allDtos, List<IntegrityError> integrityErrors) {
        requireNonNull(integrityErrors, "A list is required").addAll(DatabaseIntegrityChecker.load(allDtos).checkAllContentsObjects());
    }

    private static List<DatabaseIntegrityErrorDto> toDatabaseIntegrityErrors(List<IntegrityError> integrityErrors) {
        return integrityErrors.stream()

                .map(DatabaseIntegrityErrorDto::fromIntegrityError)

                .collect(toList());
    }
}