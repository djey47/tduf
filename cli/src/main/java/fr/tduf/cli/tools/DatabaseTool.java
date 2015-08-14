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
import fr.tduf.libunlimited.high.files.db.interop.TdumtPatchConverter;
import fr.tduf.libunlimited.high.files.db.interop.TdupePerformancePackConverter;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.PatchGenerator;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ReferenceRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.JsonGateway;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static fr.tduf.cli.tools.DatabaseTool.Command.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Command line interface for handling TDU database.
 */
public class DatabaseTool extends GenericTool {

    @Option(name = "-d", aliases = "--databaseDir", usage = "TDU database directory, defaults to current directory.")
    private String databaseDirectory;

    @Option(name = "-j", aliases = "--jsonDir", usage = "Source/Target directory for JSON files. When dump or unpack-all, defaults to current directory\\tdu-database-dump.")
    private String jsonDirectory;

    @Option(name = "-o", aliases = "--outputDatabaseDir", usage = "Fixed/Patched TDU database directory, defaults to .\\tdu-database-fixed or .\\tdu-database-patched.")
    private String outputDatabaseDirectory;

    @Option(name = "-p", aliases = "--patchFile", usage = "File describing patch to apply/create/convert. Required for all -patch operations.")
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
        APPLY_TDUPK("apply-tdupk", "Modifies vehicle physics as described in a performance pack file from TDUPE."),
        GEN_PATCH("gen-patch", "Creates mini-patch file from selected database contents."),
        CONVERT_PATCH("convert-patch", "Converts a TDUF (JSON) Patch to TDUMT (PCH) one and vice-versa."),
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
                dump(databaseDirectory);
                return true;
            case CHECK:
                check();
                return true;
            case GEN:
                gen(databaseDirectory);
                return true;
            case FIX:
                fix();
                return true;
            case APPLY_TDUPK:
                applyPerformancePack();
                return true;
            case APPLY_PATCH:
                applyPatch();
                return true;
            case GEN_PATCH:
                genPatch();
                return true;
            case CONVERT_PATCH:
                convertPatch();
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
            if ( DUMP == command
                    || UNPACK_ALL == command) {
                jsonDirectory = "tdu-database-dump";
            } else if ( APPLY_TDUPK == command
                    || APPLY_PATCH == command
                    || REPACK_ALL == command
                    || GEN_PATCH == command) {
                throw new CmdLineException(parser, "Error: jsonDirectory is required as source database.", null);
            }
        }

        if (outputDatabaseDirectory == null) {
            if (FIX == command) {
                outputDatabaseDirectory = "tdu-database-fixed";
            } else if (APPLY_PATCH == command
                    || APPLY_TDUPK == command) {
                outputDatabaseDirectory = "tdu-database-patched";
            } else if (REPACK_ALL == command) {
                outputDatabaseDirectory = "tdu-database-repacked";
            }
        }

        if (patchFile == null
                && (APPLY_TDUPK == command
                || APPLY_PATCH == command
                || CONVERT_PATCH == command)) {
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
                APPLY_TDUPK.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\vehicle.tdupk\" -r \"606298799\" -o \"C:\\Users\\Bill\\Desktop\\json-database\"",
                APPLY_PATCH.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\miniPatch.json\"",
                GEN_PATCH.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\miniPatch.json\" -t \"CAR_PHYSICS_DATA\" -r \"606298799,637314272\"",
                CONVERT_PATCH.label + " -p \"C:\\Users\\Bill\\Desktop\\install.PCH\"",
                UNPACK_ALL.label + " -d \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\" -j \"C:\\Users\\Bill\\Desktop\\json-database\"",
                REPACK_ALL.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -o \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\""
        );
    }

    private void repackAll() throws IOException {
        String sourceDirectory = Paths.get(jsonDirectory).toAbsolutePath().toString();
        Path targetPath = Paths.get(outputDatabaseDirectory).toAbsolutePath();
        outLine("-> JSON database directory: " + sourceDirectory);
        outLine("Generating TDU database files, please wait...");

        String sourceExtractedDatabaseDirectory = DatabaseReadWriteHelper.createTempDirectory();
        gen(sourceExtractedDatabaseDirectory);

        outLine("Repacking TDU database files, please wait...");

        Files.createDirectories(targetPath);
        String targetDirectory = targetPath.toString();
        DatabaseBankHelper.repackDatabaseFromDirectory(sourceExtractedDatabaseDirectory, targetDirectory, Optional.of(jsonDirectory), bankSupport);

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<> ();
        resultInfo.put("sourceDirectory", sourceDirectory);
        resultInfo.put("targetDirectory", targetDirectory);
        resultInfo.put("temporaryDirectory", sourceExtractedDatabaseDirectory);
        commandResult = resultInfo;
    }

    private void unpackAll() throws IOException {
        String sourceDirectory = Paths.get(databaseDirectory).toAbsolutePath().toString();
        outLine("-> TDU database directory: " + sourceDirectory);
        outLine("Unpacking TDU database to " + jsonDirectory + ", please wait...");

        String extractedDatabaseDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(sourceDirectory, Optional.of(jsonDirectory), bankSupport);

        outLine("Done!");

        dump(extractedDatabaseDirectory);

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("sourceDirectory", sourceDirectory);
        resultInfo.put("temporaryDirectory", extractedDatabaseDirectory);
        resultInfo.put("targetDirectory", jsonDirectory);
        commandResult = resultInfo;
    }

    private void convertPatch() throws IOException, SAXException, ParserConfigurationException, URISyntaxException, TransformerException {
        boolean tdufSource = EXTENSION_JSON.equalsIgnoreCase(com.google.common.io.Files.getFileExtension(this.patchFile));

        Path patchPath = Paths.get(this.patchFile);

        String outputExtension = tdufSource ? "pch" : EXTENSION_JSON;
        String outputPatchFile = Paths.get(patchPath.getParent().toString(), com.google.common.io.Files.getNameWithoutExtension(this.patchFile) + "." + outputExtension).toString();

        outLine("-> Source patch file: " + this.patchFile);
        outLine("Converting patch, please wait...");

        File patch = new File(this.patchFile);
        String convertOutput;
        if (tdufSource) {
            convertOutput = convertPatchFileToXML(patch);
        } else {
            convertOutput = convertPatchFileToJSON(patch);
        }

        outLine("Writing patch to " + outputPatchFile + "...");

        Files.write(Paths.get(outputPatchFile), convertOutput.getBytes(), StandardOpenOption.CREATE);

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("patchFile", outputPatchFile);
        resultInfo.put("convertedContents", convertOutput);
        commandResult = resultInfo;
    }

    private void genPatch() throws ReflectiveOperationException, IOException {
        outLine("-> Source database directory: " + this.jsonDirectory);

        outLine("Reading database, please wait...");

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

    private void applyPerformancePack() throws IOException {
        outLine("-> Source database directory: " + jsonDirectory);
        outLine("-> TDUPE Performance Pack file: " + patchFile);

        outLine("Reading database, please wait...");

        List<DbDto> allTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(jsonDirectory);

        outLine("Patching TDU database, please wait...");

        List<String> writtenFileNames = new ArrayList<>();
        BulkDatabaseMiner.load(allTopicObjects).getDatabaseTopic(CAR_PHYSICS_DATA)

                .ifPresent(
                        (carPhysicsDataTopicObject) -> applyPerformancePackToCarPhysicsData(allTopicObjects, carPhysicsDataTopicObject, writtenFileNames));

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("writtenFiles", writtenFileNames);
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

    private void dump(String databaseDirectory) throws IOException {
        FilesHelper.createDirectoryIfNotExists(jsonDirectory);

        outLine("-> Source directory: " + databaseDirectory);
        outLine("Dumping TDU database to JSON, please wait...");
        outLine();

        List<DbDto.Topic> missingTopicContents = new ArrayList<>();
        List<String> writtenFileNames = JsonGateway.dump(databaseDirectory, jsonDirectory, withClearContents, missingTopicContents);

        outLine("All done!");

        HashMap<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("missingTopicContents", missingTopicContents);
        resultInfo.put("writtenFiles", writtenFileNames);
        commandResult = resultInfo;
    }

    private void gen(String targetExtractedDatabaseDirectory) throws IOException {
        FilesHelper.createDirectoryIfNotExists(targetExtractedDatabaseDirectory);

        outLine("-> Source directory: " + jsonDirectory);
        outLine("Generating TDU database from JSON, please wait...");
        outLine();

        List<DbDto.Topic> missingTopicContents = new ArrayList<>();
        List<String> writtenFileNames = JsonGateway.gen(jsonDirectory, targetExtractedDatabaseDirectory, withClearContents, missingTopicContents);

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
                outLine("-> Now processing topic: " + databaseObject.getTopic() + "...");

                List<String> writtenFiles = DatabaseReadWriteHelper.writeDatabaseTopic(databaseObject, this.outputDatabaseDirectory, this.withClearContents);

                outLine("Writing done for topic: " + databaseObject.getTopic());
                writtenFiles.stream()
                        .forEach((fileName) -> outLine("-> " + fileName));
                outLine();
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

    private String convertPatchFileToJSON(File patch) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document patchDocument = docBuilder.parse(patch);

        DbPatchDto patchObject = TdumtPatchConverter.pchToJson(patchDocument);

        return jsonToString(patchObject);
    }

    private void applyPerformancePackToCarPhysicsData(List<DbDto> allTopicObjects, DbDto carPhysicsDataTopicObject, List<String> writtenFileNames) {
        String packLine = readLineFromPerformancePack(patchFile);
        checkCarPhysicsDataLine(packLine);

        Optional<String> potentialCarPhysicsRef = Optional.empty();
        DbPatchDto patchObject = TdupePerformancePackConverter.tdupkToJson(packLine, potentialCarPhysicsRef, carPhysicsDataTopicObject);

        try {
            AbstractDatabaseHolder.prepare(DatabasePatcher.class, allTopicObjects).apply(patchObject);
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Unable to apply patch.", roe);
        }

        outLine("Writing patched database to " + outputDatabaseDirectory + ", please wait...");

        writtenFileNames.addAll(DatabaseReadWriteHelper.writeDatabaseTopicsToJson(allTopicObjects, outputDatabaseDirectory));
    }

    private String convertPatchFileToXML(File patch) throws IOException, ParserConfigurationException, URISyntaxException, SAXException, TransformerException {
        DbPatchDto patchObject = new ObjectMapper().readValue(patch, DbPatchDto.class);

        Document patchDocument = TdumtPatchConverter.jsonToPch(patchObject);

        String documentString = xmlDocumentToString(patchDocument);

        return documentString.replaceAll("&#9;", "\t");
    }

    private static String xmlDocumentToString(Document patchDocument) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(patchDocument), new StreamResult(writer));
        return writer.toString();
    }

    private String jsonToString(DbPatchDto patchObject) throws IOException {
        ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
        return objectWriter.writeValueAsString(patchObject);
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

    static void checkCarPhysicsDataLine(String carPhysicsDataLine) {
        Pattern linePattern = Pattern.compile("^([0-9\\-\\.,]*;){103}$");

        if (!linePattern.matcher(carPhysicsDataLine).matches()) {
            throw new RuntimeException("Unrecognized Car Physics line: " + carPhysicsDataLine);
        }
    }

    private static void betweenTopicsCheck(List<DbDto> allDtos, List<IntegrityError> integrityErrors) throws ReflectiveOperationException {
        requireNonNull(integrityErrors, "A list is required").addAll(AbstractDatabaseHolder.prepare(DatabaseIntegrityChecker.class, allDtos).checkAllContentsObjects());
    }

    private static List<DatabaseIntegrityErrorDto> toDatabaseIntegrityErrors(List<IntegrityError> integrityErrors) {
        return integrityErrors.stream()

                .map(DatabaseIntegrityErrorDto::fromIntegrityError)

                .collect(toList());
    }

    private static String readLineFromPerformancePack(String ppFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(ppFile));
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to read performance pack file: " + ppFile, ioe);
        }

        return lines.get(0);
    }

    /**
     * Used by integ tests.
     */
    @VisibleForTesting
    public void setBankSupport(BankSupport bankSupport) {
        this.bankSupport = bankSupport;
    }
}