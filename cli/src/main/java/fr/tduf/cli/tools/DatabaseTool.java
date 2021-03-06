package fr.tduf.cli.tools;

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
import fr.tduf.libunlimited.high.files.db.interop.tdupe.TdupeGateway;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.high.files.db.patcher.DiffPatchesGenerator;
import fr.tduf.libunlimited.high.files.db.patcher.PatchGenerator;
import fr.tduf.libunlimited.high.files.db.patcher.domain.DatabasePatchProperties;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.JsonGateway;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseBankHelper;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.tduf.cli.tools.DatabaseTool.Command.*;
import static fr.tduf.libunlimited.high.files.db.interop.TdumtPatchConverter.EXTENSION_PCH;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper.EXTENSION_JSON;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedSet;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.*;

/**
 * Command line interface for handling TDU database.
 */
public class DatabaseTool extends GenericTool {

    @Option(name = "-d", aliases = "--databaseDir", usage = "TDU database directory, defaults to current directory.")
    private String databaseDirectory;

    @Option(name = "-j", aliases = "--jsonDir", usage = "Source/Target directory for JSON files. When dump or unpack-all, defaults to current directory\\tdu-database-dump.")
    private String jsonDirectory;

    @Option(name = "-J", aliases = "--jsonRefDir", usage = "Reference directory for JSON files.")
    private String jsonReferenceDirectory;

    @Option(name = "-o", aliases = "--outputDatabaseDir", usage = "Fixed/Patched TDU database directory, defaults to .\\tdu-database-fixed or .\\tdu-database-patched.")
    private String outputDatabaseDirectory;

    @Option(name = "-p", aliases = "--patchFile", usage = "File describing patch (or directory containing patches) to apply/create/convert. Required for all -patch operations.")
    private String patchFile;

    @Option(name = "-t", aliases = "--topic", usage = "Database topic to generate patch when gen-patch operation. Allowed values: ACHIEVEMENTS,AFTER_MARKET_PACKS,BOTS,BRANDS,CAR_COLORS,CAR_PACKS,CAR_PHYSICS_DATA,CAR_RIMS,CAR_SHOPS,CLOTHES,HAIR,HOUSES,INTERIOR,MENUS,PNJ,RIMS,SUB_TITLES,TUTORIALS.")
    private String databaseTopic;
    private DbDto.Topic effectiveTopic;

    @Option(name = "-r", aliases = "--refRange", usage = "REF of entries to create patch for. Can be a comma-separated list or a range <minValue>..<maxValue>. Not mandatory, defaults to all entries in topic.")
    private String refRange;
    private ItemRange effectiveRefRange;

    @Option(name = "-f", aliases = "--fieldRange", usage = "rank of entry fields to create patch for. Can be a comma-separated list or a range <minValue>..<maxValue>. Not mandatory, defaults to all fields in entry.")
    private String fieldRange;
    private ItemRange effectiveFieldRange;

    @Option(name = "-m", aliases = {"--mend", "--fix"}, usage = "Instructs to fix integrity errors when unpack-all operation. Not mandatory.")
    private boolean fixErrors = false;

    @Option(name = "-x", aliases = {"--extensiveCheck"}, usage = "Will process a deeper integrity check, will be slower. Not mandatory.")
    private boolean extensiveCheck = false;

    private BankSupport bankSupport;

    private Command command;


    /**
     * All available commands
     */
    enum Command implements CommandHelper.CommandEnum {
        APPLY_PATCH("apply-patch", "Modifies database contents and resources as described in a JSON mini patch file."),
        APPLY_PATCHES("apply-patches", "Modifies database contents and resources as described in JSON mini patch files from a directory."),
        APPLY_TDUPK("apply-tdupk", "Modifies vehicle physics as described in a performance pack file from TDUPE."),
        GEN_PATCH("gen-patch", "Creates mini-patch file from selected database contents."),
        DIFF_PATCHES("diff-patches", "Creates mini-patch files with differences from JSON database against reference one."),
        CONVERT_PATCH("convert-patch", "Converts a TDUF (JSON) Patch to TDUMT (PCH) one and vice-versa."),
        UNPACK_ALL("unpack-all", "Extracts full database contents from BNK to JSON files. Checks for integrity errors and optionally fixes them."),
        REPACK_ALL("repack-all", "Repacks full database from JSON files into BNK ones.");

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

    DatabaseTool() {
        bankSupport = new GenuineBnkGateway(new CommandLineHelper());
    }

    @Override
    protected boolean commandDispatch() throws Exception {
        switch (command) {
            case APPLY_TDUPK:
                commandResult = applyPerformancePack(patchFile, jsonDirectory, outputDatabaseDirectory);
                return true;
            case APPLY_PATCH:
                commandResult = applyPatch(patchFile, jsonDirectory, outputDatabaseDirectory);
                return true;
            case APPLY_PATCHES:
                commandResult = applyPatches(patchFile, jsonDirectory, outputDatabaseDirectory);
                return true;
            case GEN_PATCH:
                commandResult = genPatch(jsonDirectory, patchFile);
                return true;
            case DIFF_PATCHES:
                commandResult = diffPatches(jsonDirectory, jsonReferenceDirectory, patchFile);
                return true;
            case CONVERT_PATCH:
                commandResult = convertPatch(patchFile);
                return true;
            case UNPACK_ALL:
                commandResult = unpackAll(databaseDirectory, jsonDirectory, fixErrors, extensiveCheck);
                return true;
            case REPACK_ALL:
                commandResult = repackAll(jsonDirectory, outputDatabaseDirectory);
                return true;
            default:
                commandResult = null;
                return false;
        }
    }

    @Override
    protected void assignCommand(String commandArgument) {
        command = (Command) CommandHelper.fromLabel(getCommand(), commandArgument);
    }

    @Override
    protected void checkAndAssignDefaultParameters(CmdLineParser parser) throws CmdLineException {
        if (databaseDirectory == null) {
            databaseDirectory = ".";
        }

        if (jsonDirectory == null) {
            if (UNPACK_ALL == command) {
                jsonDirectory = "tdu-database-dump";
            } else if (APPLY_TDUPK == command
                    || APPLY_PATCH == command
                    || APPLY_PATCHES == command
                    || REPACK_ALL == command
                    || GEN_PATCH == command
                    || DIFF_PATCHES == command) {
                throw new CmdLineException(parser, "Error: jsonDirectory is required as source database.", null);
            }
        }

        if (jsonReferenceDirectory == null
                && DIFF_PATCHES == command) {
            throw new CmdLineException(parser, "Error: jsonReferenceDirectory is required as reference database.", null);
        }

        if (outputDatabaseDirectory == null) {
            if (APPLY_PATCH == command
                    || APPLY_TDUPK == command) {
                outputDatabaseDirectory = "tdu-database-patched";
            } else if (REPACK_ALL == command) {
                outputDatabaseDirectory = "tdu-database-repacked";
            }
        }

        if (patchFile == null
                && (APPLY_TDUPK == command
                || APPLY_PATCH == command
                || APPLY_PATCHES == command
                || CONVERT_PATCH == command
                || DIFF_PATCHES == command)) {
            throw new CmdLineException(parser, "Error: patchFile is required.", null);
        }

        if (GEN_PATCH == command) {
            if (databaseTopic == null) {
                throw new CmdLineException(parser, "Error: database topic is required.", null);
            }
            effectiveTopic = DbDto.Topic.valueOf(databaseTopic);
            effectiveRefRange = ItemRange.fromCliOption(refRange);
            effectiveFieldRange = ItemRange.fromCliOption(fieldRange);
        }
    }

    @Override
    protected CommandHelper.CommandEnum getCommand() {
        return Command.UNPACK_ALL;
    }

    @Override
    protected List<String> getExamples() {
        return asList(
                APPLY_TDUPK.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\vehicle.tdupk\" -r \"606298799\" -o \"C:\\Users\\Bill\\Desktop\\json-database\"",
                APPLY_PATCH.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\miniPatch.json\"",
                APPLY_PATCHES.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\\"",
                GEN_PATCH.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -p \"C:\\Users\\Bill\\Desktop\\miniPatch.json\" -t \"CAR_PHYSICS_DATA\" -r \"606298799,637314272\" -f \"102,103\"",
                DIFF_PATCHES.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -J \"C:\\Users\\Bill\\Desktop\\json-database-reference\" -p \"C:\\Users\\Bill\\Desktop\"",
                CONVERT_PATCH.label + " -p \"C:\\Users\\Bill\\Desktop\\install.PCH\"",
                UNPACK_ALL.label + " -d \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\" -j \"C:\\Users\\Bill\\Desktop\\json-database\" -m",
                REPACK_ALL.label + " -j \"C:\\Users\\Bill\\Desktop\\json-database\" -o \"C:\\Program Files (x86)\\Test Drive Unlimited\\Euro\\Bnk\\Database\""
        );
    }

    private Map<String, ?> repackAll(String jsonSourceDirectory, String targetDatabaseDirectory) throws IOException {
        String sourceDirectory = Paths.get(jsonSourceDirectory).toAbsolutePath().toString();
        Path targetPath = Paths.get(targetDatabaseDirectory).toAbsolutePath();
        outLine("-> JSON database directory: " + sourceDirectory);
        outLine("Generating TDU database files, please wait...");

        String sourceExtractedDatabaseDirectory = DatabaseReadWriteHelper.createTempDirectory();
        generateDatabaseFiles(jsonSourceDirectory, sourceExtractedDatabaseDirectory);

        outLine("Repacking TDU database files, please wait...");

        Files.createDirectories(targetPath);
        String targetDirectory = targetPath.toString();
        DatabaseBankHelper.repackDatabaseFromDirectory(sourceExtractedDatabaseDirectory, targetDirectory, jsonSourceDirectory, bankSupport);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("sourceDirectory", sourceDirectory);
        resultInfo.put("targetDirectory", targetDirectory);
        resultInfo.put("temporaryDirectory", sourceExtractedDatabaseDirectory);

        return resultInfo;
    }

    private Map<String, ?> unpackAll(String sourceDatabaseDirectory, String jsonDatabaseDirectory, boolean fixErrors, boolean extensiveCheck) throws Exception {
        String sourceDirectory = Paths.get(sourceDatabaseDirectory).toAbsolutePath().toString();
        outLine("-> TDU database directory: " + sourceDirectory);
        outLine("Unpacking TDU database to " + jsonDatabaseDirectory + ", please wait...");

        String extractedDatabaseDirectory = DatabaseBankHelper.unpackDatabaseFromDirectory(sourceDirectory, jsonDatabaseDirectory, bankSupport);

        outLine("Done unpacking.");

        Set<IntegrityError> integrityErrors = new LinkedHashSet<>();
        List<DbDto.Topic> missingTopicContents = new ArrayList<>();
        final List<String> writtenFileNames = convertDatabaseFilesToJson(extractedDatabaseDirectory, jsonDatabaseDirectory, missingTopicContents, integrityErrors);

        List<DbDto> databaseObjects = new ArrayList<>();
        outLine("-> JSON database directory: " + jsonDatabaseDirectory);
        if (extensiveCheck) {
            outLine("Now checking database...");
            databaseObjects = loadAndCheckDatabase(extractedDatabaseDirectory, integrityErrors);

            outLine("  .Database check just ended: " + integrityErrors.size() + " error(s).");
        } else {
            outLine("Now loading database...");
            try {
                databaseObjects = loadDatabaseFromJsonFiles(jsonDatabaseDirectory);
            } catch (IllegalArgumentException ignored) {
            }

            outLine("  .Database loading just ended: " + integrityErrors.size() + " error(s).");
        }

        outLine();
        printIntegrityErrors(integrityErrors);

        Map<String, Object> resultInfo = new HashMap<>();
        if (fixErrors
                && !integrityErrors.isEmpty()) {
            outLine("-> JSON database directory: " + jsonDatabaseDirectory);
            outLine("Now fixing database...");
            Set<IntegrityError> remainingIntegrityErrors = fixIntegrityErrorsAndSaveDatabaseFiles(databaseObjects, integrityErrors, jsonDatabaseDirectory);
            outLine("Done fixing.");

            resultInfo.put("remainingIntegrityErrors", toDatabaseIntegrityErrors(remainingIntegrityErrors));
        }
        outLine();

        resultInfo.put("sourceDatabaseDirectory", sourceDirectory);
        resultInfo.put("temporaryDirectory", extractedDatabaseDirectory);
        resultInfo.put("jsonDatabaseDirectory", jsonDatabaseDirectory);
        resultInfo.put("writtenFiles", writtenFileNames);
        resultInfo.put("missingTopicContents", missingTopicContents);
        resultInfo.put("integrityErrors", integrityErrors);

        if (!integrityErrors.isEmpty()) {
            commandResult = resultInfo;
            throw new Exception("Integrity check found errors!");
        }

        return resultInfo;
    }

    private Map<String, ?> convertPatch(String sourcePatchFile) throws IOException, SAXException, ParserConfigurationException, URISyntaxException, TransformerException {
        boolean tdufSource = EXTENSION_JSON.equalsIgnoreCase(FilesHelper.getExtension(sourcePatchFile));

        Path patchPath = Paths.get(sourcePatchFile);

        // Extract pch to lib constant
        String outputExtension = tdufSource ? EXTENSION_PCH : EXTENSION_JSON;
        String outputPatchFile = Paths.get(patchPath.getParent().toString(), FilesHelper.getNameWithoutExtension(sourcePatchFile) + "." + outputExtension).toString();

        outLine("-> Source patch file: " + sourcePatchFile);
        outLine("Converting patch, please wait...");

        File patch = new File(sourcePatchFile);
        String convertOutput;
        if (tdufSource) {
            convertOutput = convertPatchFileToXML(patch);
        } else {
            convertOutput = convertPatchFileToJSON(patch);
        }

        outLine("Writing patch to " + outputPatchFile + "...");

        Files.write(Paths.get(outputPatchFile), convertOutput.getBytes());

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("patchFile", outputPatchFile);
        resultInfo.put("convertedContents", convertOutput);

        return resultInfo;
    }

    private Map<String, ?> genPatch(String sourceJsonDirectory, String targetPatchFile) throws ReflectiveOperationException, IOException {
        outLine("-> Source database directory: " + sourceJsonDirectory);

        outLine("Reading database, please wait...");

        List<DbDto> allTopicObjects = loadDatabaseFromJsonFiles(sourceJsonDirectory);

        outLine("Generating patch, please wait...");

        DbPatchDto patchObject = AbstractDatabaseHolder.prepare(PatchGenerator.class, allTopicObjects).makePatch(effectiveTopic, effectiveRefRange, effectiveFieldRange);

        writePatchToFile(patchObject, targetPatchFile);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("patchFile", targetPatchFile);

        return resultInfo;
    }

    private Map<String, ?> diffPatches(String jsonSourceDirectory, String jsonReferenceDirectory, String targetPatchesDirectory) throws ReflectiveOperationException {
        outLine("-> Source database directory: " + jsonSourceDirectory);
        outLine("-> Reference database directory: " + jsonReferenceDirectory);

        outLine("Reading source database, please wait...");

        List<DbDto> currentTopicObjects = loadDatabaseFromJsonFiles(jsonSourceDirectory);

        outLine("Reading source database, please wait...");

        List<DbDto> referenceTopicObjects = loadDatabaseFromJsonFiles(jsonReferenceDirectory);

        outLine("Generating patches, please wait...");

        final Set<DbPatchDto> diffPatchObjects = DiffPatchesGenerator.prepare(currentTopicObjects, referenceTopicObjects).makePatches();

        Set<String> targetPatchFiles = writePatches(diffPatchObjects, targetPatchesDirectory);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("writtenFiles", targetPatchFiles);

        return resultInfo;
    }

    private Map<String, ?> applyPerformancePack(String performancePackFile, String sourceJsonDirectory, String targetJsonDirectory) throws IOException {
        FilesHelper.createDirectoryIfNotExists(targetJsonDirectory);

        outLine("-> Source database directory: " + sourceJsonDirectory);
        outLine("-> TDUPE Performance Pack file: " + performancePackFile);

        outLine("Reading database, please wait...");

        List<DbDto> allTopicObjects = loadDatabaseFromJsonFiles(sourceJsonDirectory);

        outLine("Patching TDU database, please wait...");

        List<String> writtenFileNames = new ArrayList<>();
        BulkDatabaseMiner.load(allTopicObjects).getDatabaseTopic(CAR_PHYSICS_DATA)

                .ifPresent(
                        (carPhysicsDataTopicObject) -> applyPerformancePackToCarPhysicsData(performancePackFile, targetJsonDirectory, allTopicObjects, writtenFileNames));

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("writtenFiles", writtenFileNames);

        return resultInfo;
    }

    private Map<String, ?> applyPatch(String sourcePatchFile, String sourceJsonDirectory, String targetDatabaseDirectory) throws IOException, ReflectiveOperationException {
        FilesHelper.createDirectoryIfNotExists(targetDatabaseDirectory);

        outLine("-> Source database directory: " + sourceJsonDirectory);
        outLine("-> Mini patch file: " + sourcePatchFile);

        DatabasePatchProperties patchProperties = readPatchProperties(sourcePatchFile);

        outLine("Patching TDU database, please wait...");

        DbPatchDto patchObject = jsonMapper.readValue(new File(sourcePatchFile), DbPatchDto.class);

        List<DbDto> allTopicObjects = loadDatabaseFromJsonFiles(sourceJsonDirectory);
        final DatabasePatchProperties effectivePatchProperties = AbstractDatabaseHolder.prepare(DatabasePatcher.class, allTopicObjects).applyWithProperties(patchObject, patchProperties);

        outLine("Writing patched database to " + targetDatabaseDirectory + ", please wait...");

        List<String> writtenFileNames = DatabaseReadWriteHelper.writeDatabaseTopicsToJson(allTopicObjects, targetDatabaseDirectory);

        String writtenPropertyFile = writePatchProperties(effectivePatchProperties, sourcePatchFile);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("writtenFiles", writtenFileNames);
        resultInfo.put("effectivePatchPropertyFile", writtenPropertyFile);

        return resultInfo;
    }

    private Map<String, ?> applyPatches(String sourcePatchesDirectory, String sourceJsonDirectory, String targetDatabaseDirectory) throws IOException, ReflectiveOperationException {
        FilesHelper.createDirectoryIfNotExists(targetDatabaseDirectory);

        outLine("-> Source database directory: " + sourceJsonDirectory);
        outLine("-> Mini patches directory: " + sourcePatchesDirectory);

        outLine("Patching TDU database, please wait...");

        List<DbDto> allTopicObjects = loadDatabaseFromJsonFiles(sourceJsonDirectory);

        final Map<DbPatchDto, DatabasePatchProperties> patchObjectsAndProps = Files.walk(Paths.get(sourcePatchesDirectory))
                .filter(Files::isRegularFile)
                .filter(path -> EXTENSION_JSON.equalsIgnoreCase(FilesHelper.getExtension(path.toString())))
                .sorted(Path::compareTo)
                .collect(toMap(
                        patchPath -> {
                            outLine("-> Mini patch file: " + patchPath.toString());

                            try {
                                return jsonMapper.readValue(patchPath.toFile(), DbPatchDto.class);
                            } catch (IOException ioe) {
                                throw new RuntimeException(ioe);
                            }
                        },
                        patchPath -> {
                            try {
                                return readPatchProperties(patchPath.toString());
                            } catch (IOException ioe) {
                                throw new RuntimeException(ioe);
                            }
                        },
                        (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                        LinkedHashMap::new));

        AbstractDatabaseHolder.prepare(DatabasePatcher.class, allTopicObjects).batchApplyWithProperties(patchObjectsAndProps);

        // Effective properties are not handled for now

        outLine("Writing patched database to " + targetDatabaseDirectory + ", please wait...");

        List<String> writtenFileNames = DatabaseReadWriteHelper.writeDatabaseTopicsToJson(allTopicObjects, targetDatabaseDirectory);

        Map<String, Object> resultInfo = new HashMap<>();
        resultInfo.put("writtenFiles", writtenFileNames);

        return resultInfo;
    }

    private DatabasePatchProperties readPatchProperties(String patchFile) throws IOException {
        String propertyFile = patchFile + ".properties";

        final DatabasePatchProperties patchProperties = new DatabasePatchProperties();
        final File propertyFileHandle = new File(propertyFile);
        if (propertyFileHandle.exists()) {

            outLine("-> Patch properties: " + propertyFile);

            final InputStream inputStream = new FileInputStream(propertyFileHandle);
            patchProperties.load(inputStream);

        } else {

            outLine("-> Patch properties: not provided");

        }

        return patchProperties;
    }

    private String writePatchProperties(DatabasePatchProperties patchProperties, String patchFile) throws IOException {
        if (patchProperties.isEmpty()) {
            return "N/A";
        }

        final Path patchPath = Paths.get(patchFile);
        Path patchParentPath = patchPath.getParent();
        String patchFileName = patchPath.getFileName().toString();
        final String targetFileName = "effective-" + patchFileName + ".properties";
        String targetPropertyFile = patchParentPath.resolve(targetFileName).toString();

        outLine("Writing properties to " + targetPropertyFile + ", please wait...");

        final OutputStream outputStream = new FileOutputStream(targetPropertyFile);
        patchProperties.store(outputStream, null);

        return targetPropertyFile;
    }

    private void generateDatabaseFiles(String sourceJsonDirectory, String targetExtractedDatabaseDirectory) throws IOException {
        FilesHelper.createDirectoryIfNotExists(targetExtractedDatabaseDirectory);

        outLine("-> Source directory: " + sourceJsonDirectory);
        outLine("Generating TDU database from JSON, please wait...");
        outLine();

        List<DbDto.Topic> missingTopicContents = new ArrayList<>();
        JsonGateway.gen(sourceJsonDirectory, targetExtractedDatabaseDirectory, missingTopicContents);
    }

    private Set<IntegrityError> fixIntegrityErrorsAndSaveDatabaseFiles(List<DbDto> databaseObjects, Set<IntegrityError> integrityErrors, String jsonDatabaseDirectory) throws ReflectiveOperationException {
        DatabaseIntegrityFixer databaseIntegrityFixer = AbstractDatabaseHolder.prepare(DatabaseIntegrityFixer.class, databaseObjects);
        Set<IntegrityError> remainingIntegrityErrors = databaseIntegrityFixer.fixAllContentsObjects(integrityErrors);

        List<DbDto> fixedDatabaseObjects = databaseIntegrityFixer.getDatabaseObjects();
        DatabaseReadWriteHelper.writeDatabaseTopicsToJson(fixedDatabaseObjects, jsonDatabaseDirectory);

        printIntegrityErrors(remainingIntegrityErrors);
        return remainingIntegrityErrors;
    }

    private List<String> convertDatabaseFilesToJson(String databaseDirectory, String targetJsonDirectory, List<DbDto.Topic> missingTopicContents, Set<IntegrityError> integrityErrors) throws IOException {
        FilesHelper.createDirectoryIfNotExists(targetJsonDirectory);

        outLine("-> Source directory: " + databaseDirectory);
        outLine("Dumping TDU database to JSON, please wait...");
        outLine();

        return JsonGateway.dump(databaseDirectory, targetJsonDirectory, missingTopicContents, integrityErrors);
    }

    private String convertPatchFileToJSON(File patchFile) throws ParserConfigurationException, SAXException, IOException {
        Document patchDocument = FilesHelper.readXMLDocumentFromFile(patchFile.getPath());

        DbPatchDto patchObject = TdumtPatchConverter.pchToJson(patchDocument);

        return jsonWriter.writeValueAsString(patchObject);
    }

    private void applyPerformancePackToCarPhysicsData(String performancePackFile, String targetJsonDirectory, List<DbDto> allTopicObjects, List<String> writtenFileNames) {
        try {
            TdupeGateway gateway = AbstractDatabaseHolder.prepare(TdupeGateway.class, allTopicObjects);
            gateway.applyPerformancePackToEntryWithReference(refRange, performancePackFile);
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException("Unable to apply patch.", roe);
        }

        outLine("Writing patched database to " + targetJsonDirectory + ", please wait...");

        writtenFileNames.addAll(DatabaseReadWriteHelper.writeDatabaseTopicsToJson(allTopicObjects, targetJsonDirectory));
    }

    private String convertPatchFileToXML(File patch) throws IOException, ParserConfigurationException, URISyntaxException, SAXException, TransformerException {
        DbPatchDto patchObject = jsonMapper.readValue(patch, DbPatchDto.class);

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

    private List<DbDto> loadDatabaseFromJsonFiles(String sourceJsonDirectory) {
        List<DbDto> allTopicObjects = DatabaseReadWriteHelper.readFullDatabaseFromJson(sourceJsonDirectory);
        if (allTopicObjects.isEmpty()) {
            throw new IllegalArgumentException("No database topic found in specified JSON directory.");
        }

        return allTopicObjects;
    }

    private List<DbDto> loadAndCheckDatabase(String sourceDatabaseDirectory, Set<IntegrityError> integrityErrors) throws IOException, ReflectiveOperationException {
        requireNonNull(integrityErrors, "An empty set of integrity errors is required");

        Set<IntegrityError> integrityErrorsWhileLoading = synchronizedSet(integrityErrors);
        final List<DbDto> readObjects = DbDto.Topic.valuesAsStream()

                .parallel()

                .map((currentTopic) -> loadSingleTopicWithBasicChecking(currentTopic, sourceDatabaseDirectory, integrityErrorsWhileLoading))

                .filter(Optional::isPresent)

                .map(Optional::get)

                .collect(toList());

        outLine("  -> Now performing extensive check...");
        Set<IntegrityError> integrityErrorsWhileExtensiveChecking = DatabaseIntegrityChecker.prepare(DatabaseIntegrityChecker.class, readObjects).checkAllContentsObjects();

        integrityErrors.addAll(integrityErrorsWhileLoading);
        integrityErrors.addAll(integrityErrorsWhileExtensiveChecking);

        return readObjects;
    }

    private Optional<DbDto> loadSingleTopicWithBasicChecking(DbDto.Topic currentTopic, String sourceDatabaseDirectory, Set<IntegrityError> integrityErrorsWhileProcessing) {
        outLine("  -> Now loading topic: " + currentTopic + "...");

        int initialErrorCount = integrityErrorsWhileProcessing.size();

        Optional<DbDto> potentialDbDto = empty();
        try {
            potentialDbDto = DatabaseReadWriteHelper.readDatabaseTopic(currentTopic, sourceDatabaseDirectory, integrityErrorsWhileProcessing);
        } catch (IOException e) {
            outLine("  (!)Database contents could not be read for topic " + currentTopic + ", skipping.");
            outLine();
            return potentialDbDto;
        }

        if (potentialDbDto.isPresent()) {
            DbDto dbDto = potentialDbDto.get();
            outLine("  .Read topic with basic checking: " + currentTopic + ", " + (integrityErrorsWhileProcessing.size() - initialErrorCount) + " error(s).");
            outLine("  .Content line count: " + dbDto.getData().getEntries().size());
            outLine("  .Resource entry count: " + dbDto.getResource().getEntries().size());
        } else {
            outLine("  (!)Database contents not found for topic " + currentTopic + ", skipping.");
        }
        outLine();

        return potentialDbDto;
    }

    private void printIntegrityErrors(Set<IntegrityError> integrityErrors) {
        if (integrityErrors.isEmpty()) {
            return;
        }

        outLine("-> Integrity errors (" + integrityErrors.size() + "):");

        integrityErrors.forEach(
                (integrityError) -> {
                    String errorMessage = String.format(integrityError.getErrorMessageFormat(), integrityError.getInformation());
                    outLine("  (!)" + errorMessage);
                });
    }

    private Set<String> writePatches(Set<DbPatchDto> diffPatchObjects, String targetPatchesDirectory) {

        outLine("Writing patches to " + targetPatchesDirectory + "...");

        return diffPatchObjects.parallelStream()

                .map((patchObject) -> {
                    String targetPatchFile = Paths.get(targetPatchesDirectory, patchObject.getComment() + ".mini.json").toString();

                    writePatchToFile(patchObject, targetPatchFile);

                    return targetPatchFile;
                })

                .collect(toSet());
    }

    private void writePatchToFile(DbPatchDto patchObject, String targetPatchFile) {
        outLine(".Writing patch to " + targetPatchFile + "...");

        try {
            FilesHelper.writeJsonObjectToFile(patchObject, targetPatchFile);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static List<DatabaseIntegrityErrorDto> toDatabaseIntegrityErrors(Set<IntegrityError> integrityErrors) {
        return integrityErrors.stream()

                .map(DatabaseIntegrityErrorDto::fromIntegrityError)

                .collect(toList());
    }

    /**
     * Used by integ tests.
     */
    void setBankSupport(BankSupport bankSupport) {
        this.bankSupport = bankSupport;
    }
}
