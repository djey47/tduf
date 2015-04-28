package fr.tduf.cli.tools;

import com.google.common.annotations.VisibleForTesting;
import fr.tduf.cli.common.helper.CommandHelper;
import fr.tduf.cli.tools.dto.DatabaseIntegrityErrorDto;
import fr.tduf.libunlimited.common.helper.CommandLineHelper;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.banks.BankSupport;
import fr.tduf.libunlimited.high.files.banks.interop.GenuineBnkGateway;
import fr.tduf.libunlimited.high.files.db.common.AbstractDatabaseHolder;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityChecker;
import fr.tduf.libunlimited.high.files.db.integrity.DatabaseIntegrityFixer;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.PatchGenerator;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ReferenceRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.cli.tools.DatabaseTool.Command.*;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Command line interface for handling TDU database.
 */
// TODO Move part of bank processing to lib (unpackAll / repackAll)
// TODO see to remove DUMP and GEN operations when UNPACK_ALL / REPACK_ALL are ok
public class DatabaseTool extends GenericTool {

    @Option(name = "-d", aliases = "--databaseDir", usage = "TDU database directory, defaults to current directory.")
    private String databaseDirectory;

    @Option(name = "-j", aliases = "--jsonDir", usage = "Source/Target directory for JSON files. When dump or unpack-all, defaults to current directory\\tdu-database-dump.")
    private String jsonDirectory;

    @Option(name = "-o", aliases = "--outputDatabaseDir", usage = "Fixed/Patched TDU database directory, defaults to .\\tdu-database-fixed or .\\tdu-database-patched.")
    private String outputDatabaseDirectory;

    @Option(name = "-p", aliases = "--patchFile", usage = "File describing mini patch to apply/create. Required for both apply-patch and gen-patch operations.")
    private String patchFile;

    @Option(name = "-c", aliases = "--clear", usage = "Indicates unpacked TDU files do not need to be unencrypted and encrypted back. Not mandatory.")
    private boolean withClearContents = false;

    @Option(name = "-t", aliases = "--topic", usage = "Database topic to generate patch when gen-patch operation. Allowed values: ACHIEVEMENTS,AFTER_MARKET_PACKS,BOTS,BRANDS,CAR_COLORS,CAR_PACKS,CAR_PHYSICS_DATA,CAR_RIMS,CAR_SHOPS,CLOTHES,HAIR,HOUSES,INTERIOR,MENUS,PNJ,RIMS,SUB_TITLES,TUTORIALS.")
    private String databaseTopic;
    private DbDto.Topic effectiveTopic;

    @Option(name = "-r", aliases = "--range", usage = "REF of entries to create patch for. Can be a comma-separated list or a range <minValue>..<maxValue>. Not mandatory, defaults to all entries in topic.")
    private String itemsRange;
    private ReferenceRange effectiveRange;

    private BankSupport bankSupport;

    private Command command;

    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        CHECK("check", "Tries to load database and display integrity errors, if any."),
        DUMP("dump", "Writes UNPACKED full database contents to JSON files."),
        GEN("gen", "Writes UNPACKED TDU database files from JSON files."),
        FIX("fix", "Loads database, checks for integrity errors and create database copy with fixed ones."),
        APPLY_PATCH("apply-patch", "Modifies database contents and resources as described in a JSON mini patch file."),
        GEN_PATCH("gen-patch", "Creates mini-patch file from selected database contents."),
        UNPACK_ALL("unpack-all", "Extracts full database contents from BNK to JSON files."),
        REPACK_ALL("repack-all", "Repacks full database from JSON files into BNK ones." );

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

    public DatabaseTool() {
        this.bankSupport = new GenuineBnkGateway(new CommandLineHelper());
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
            case GEN_PATCH:
                genPatch();
                return true;
            case UNPACK_ALL:
                unpackAll();
                return true;
            case REPACK_ALL:
                repackAll();
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
            if (DUMP == command || UNPACK_ALL == command) {
                jsonDirectory = "tdu-database-dump";
            } else if (APPLY_PATCH == command || REPACK_ALL == command || GEN_PATCH == command) {
                throw new CmdLineException(parser, "Error: jsonDirectory is required as source database.", null);
            }
        }

        if (outputDatabaseDirectory == null) {
            if (FIX == command) {
                outputDatabaseDirectory = "tdu-database-fixed";
            } else if (APPLY_PATCH == command) {
                outputDatabaseDirectory = "tdu-database-patched";
            } else if (REPACK_ALL == command) {
                outputDatabaseDirectory = "tdu-database-repacked";
            }
        }

        if (patchFile == null && APPLY_PATCH == command) {
            throw new CmdLineException(parser, "Error: patchFile is required.", null);
        }

        if (GEN_PATCH == command) {
            if (this.databaseTopic == null) {
                throw new CmdLineException(parser, "Error: database topic is required.", null);
            }
            this.effectiveTopic = DbDto.Topic.valueOf(this.databaseTopic);
            this.effectiveRange = ReferenceRange.fromCliOption(Optional.ofNullable(this.itemsRange));
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
                APPLY_PATCH.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\miniPatch.json\"",
                GEN_PATCH.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\miniPatch.json\" -t \"CAR_PHYSICS_DATA\" -r \"606298799,637314272\"",
                UNPACK_ALL.label + " -d \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\" -j \"C:\\Users\\Bill\\Desktop\\json-database\"",
                REPACK_ALL.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -o \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\""
        );
    }

    private void repackAll() throws IOException {
        String sourceDirectory = Paths.get(this.jsonDirectory).toAbsolutePath().toString();
        String targetDirectory = Paths.get(this.outputDatabaseDirectory).toAbsolutePath().toString();
        outLine("-> JSON database directory: " + sourceDirectory);
        outLine("Generating TDU database files, please wait...");

        this.databaseDirectory = DatabaseReadWriteHelper.createTempDirectory();
        gen();

        copyOriginalBankFilesToTargetDirectory(this.jsonDirectory, this.databaseDirectory);

        outLine("Repacking TDU database files, please wait...");

        DatabaseBankHelper.repackDatabaseFromDirectory(this.databaseDirectory, targetDirectory, this.bankSupport);

        outLine("All done!");

        Map<String, Object> resultInfo = (Map<String, Object>) this.commandResult;
        resultInfo.put("sourceDirectory", sourceDirectory);
        resultInfo.put("targetDirectory", targetDirectory);
        resultInfo.put("temporaryDirectory", this.databaseDirectory);
    }

    private void unpackAll() throws IOException {
        String sourceDirectory = Paths.get(this.databaseDirectory).toAbsolutePath().toString();
        outLine("-> TDU database directory: " + sourceDirectory);
        outLine("Unpacking TDU database to " + this.jsonDirectory + ", please wait...");

        this.databaseDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(sourceDirectory, this.bankSupport);

        copyOriginalBankFilesToTargetDirectory(this.databaseDirectory, this.jsonDirectory);

        outLine("Done!");

        dump();

        Map<String, Object> resultInfo = (Map<String, Object>) this.commandResult;
        resultInfo.put("sourceDirectory", sourceDirectory);
        resultInfo.put("temporaryDirectory", this.databaseDirectory);
        resultInfo.put("targetDirectory", this.jsonDirectory);
    }

    private void genPatch() throws ReflectiveOperationException, IOException {
        outLine("-> Source database directory: " + this.jsonDirectory);

        outLine("Reading database patch, please wait...");

        List<DbDto> allTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(this.jsonDirectory);

        outLine("Generating patch, please wait...");

        DbPatchDto patchObject = AbstractDatabaseHolder.prepare(PatchGenerator.class, allTopicObjects).makePatch(this.effectiveTopic, this.effectiveRange);

        outLine("Writing patch to " + this.patchFile + "...");

        writePatchFileToDisk(patchObject);

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("patchFile", this.patchFile);
        commandResult = resultInfo;
    }

    private void applyPatch() throws IOException, ReflectiveOperationException {
        FilesHelper.createDirectoryIfNotExists(this.outputDatabaseDirectory);

        outLine("-> Source database directory: " + this.jsonDirectory);
        outLine("-> Mini patch file: " + this.patchFile);
        outLine("Patching TDU database, please wait...");

        DbPatchDto patchObject = new ObjectMapper().readValue(new File(patchFile), DbPatchDto.class);

        List<DbDto> allTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(this.jsonDirectory);
        AbstractDatabaseHolder.prepare(DatabasePatcher.class, allTopicObjects).apply(patchObject);

        outLine("Writing patched database to " + this.outputDatabaseDirectory + ", please wait...");

        List<String> writtenFileNames = DatabaseReadWriteHelper.writeDatabaseTopicsToJson(allTopicObjects, this.outputDatabaseDirectory);

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("writtenFiles", writtenFileNames);
        commandResult = resultInfo;
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

            Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopic(currentTopic, this.databaseDirectory, this.withClearContents, new ArrayList<>());
            if (!potentialDbDto.isPresent()) {
                outLine("  !Database contents not found for topic " + currentTopic + ", skipping...");
                outLine();

                missingTopicContents.add(currentTopic);

                continue;
            }

            DatabaseReadWriteHelper.writeDatabaseTopicToJson(potentialDbDto.get(), this.jsonDirectory)

                    .ifPresent((writtenFileName) -> {
                        outLine("Writing done for topic: " + currentTopic);
                        outLine("-> " + writtenFileName);
                        outLine();

                        writtenFileNames.add(writtenFileName);
                    });
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

            Optional<DbDto> dbDto = DatabaseReadWriteHelper.readDatabaseTopicFromJson(currentTopic, this.jsonDirectory);

            if (dbDto.isPresent()) {
                writtenFileNames.addAll(writeDatabaseTopic(dbDto.get(), this.databaseDirectory));
            } else {
                outLine("  !Database contents not found for topic " + currentTopic + ", skipping...");
                outLine();

                missingTopicContents.add(currentTopic);
            }
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
            outLine("At least one integrity error has been found, your database may not be ready-to-use.");
        }

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("integrityErrors", toDatabaseIntegrityErrors(integrityErrors));

        commandResult = resultInfo;
    }

    private void fix() throws IOException, ReflectiveOperationException {
        List<IntegrityError> integrityErrors = new ArrayList<>();
        List<DbDto> databaseObjects = checkAndReturnIntegrityErrorsAndObjects(integrityErrors);

        if(integrityErrors.isEmpty()) {
            outLine("No error detected - a fix is not necessary.");
            return;
        }

        outLine("-> Now fixing database...");
        DatabaseIntegrityFixer databaseIntegrityFixer = AbstractDatabaseHolder.prepare(DatabaseIntegrityFixer.class, databaseObjects);
        List<IntegrityError> remainingIntegrityErrors = databaseIntegrityFixer.fixAllContentsObjects(integrityErrors);
        List<DbDto> fixedDatabaseObjects = databaseIntegrityFixer.getDatabaseObjects();

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

    private void writePatchFileToDisk(DbPatchDto patchObject) throws IOException {
        Path patchFilePath = Paths.get(this.patchFile);
        Files.createDirectories(patchFilePath.getParent());
        try ( BufferedWriter bufferedWriter = Files.newBufferedWriter(patchFilePath, StandardCharsets.UTF_8)) {
            new ObjectMapper().writer().writeValue(bufferedWriter, patchObject);
        }
    }

    private List<DbDto> checkAndReturnIntegrityErrorsAndObjects(List<IntegrityError> integrityErrors) throws IOException, ReflectiveOperationException {
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

            Optional<DbDto> potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopic(currentTopic, this.databaseDirectory, this.withClearContents, integrityErrors);

            if (!potentialDbDto.isPresent()) {
                outLine("  (!)Database contents not found for topic " + currentTopic + ", skipping.");
                outLine();
                continue;
            }

            DbDto dbDto = potentialDbDto.get();
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

            allDtos.add(dbDto);

            outLine();
        }

        return allDtos;
    }

    private List<String> writeDatabaseTopic(DbDto dbDto, String outputDatabaseDirectory) throws IOException {
        List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabaseTopic(dbDto, outputDatabaseDirectory, this.withClearContents);

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

    private static void copyOriginalBankFilesToTargetDirectory(String sourceDirectory, String targetDirectory) throws IOException {
        Files.walk(Paths.get(sourceDirectory))

                .filter((path) -> Files.isRegularFile(path))

                .filter((filePath) -> GenuineBnkGateway.EXTENSION_BANKS.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(filePath.toString())))

                        .forEach((filePath) -> {
                            try {
                                Files.copy(filePath, Paths.get(targetDirectory, filePath.getFileName().toString()));
                            } catch (IOException ioe) {
                                throw new RuntimeException("Unable to copy original bank files to target directory.", ioe);
                    }
                });
    }

    private static void betweenTopicsCheck(List<DbDto> allDtos, List<IntegrityError> integrityErrors) throws ReflectiveOperationException {
        requireNonNull(integrityErrors, "A list is required").addAll(AbstractDatabaseHolder.prepare(DatabaseIntegrityChecker.class, allDtos).checkAllContentsObjects());
    }

    private static List<DatabaseIntegrityErrorDto> toDatabaseIntegrityErrors(List<IntegrityError> integrityErrors) {
        return integrityErrors.stream()

                .map(DatabaseIntegrityErrorDto::fromIntegrityError)

                .collect(toList());
    }

    /**
     * Used by integ tests.
     */
    @VisibleForTesting
    public void setBankSupport(BankSupport bankSupport) {
        this.bankSupport = bankSupport;
    }
}