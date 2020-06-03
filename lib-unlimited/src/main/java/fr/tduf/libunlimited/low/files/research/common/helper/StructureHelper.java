package fr.tduf.libunlimited.low.files.research.common.helper;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tduf.libunlimited.common.forever.FileConstants;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.common.helper.RegexHelper;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.common.crypto.helper.CryptoHelper;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility class to provide common operations on Structures.
 */
public class StructureHelper {
    private static final Class<StructureHelper> THIS_CLASS = StructureHelper.class;
    private static final String THIS_CLASS_NAME = THIS_CLASS.getSimpleName();

    /**
     * @param location : resource name or file location.
     * @return file structure, according to specified location
     */
    public static FileStructureDto retrieveStructureFromLocation(String location) throws IOException {
        InputStream fileStructureStream = THIS_CLASS.getResourceAsStream(location);
        ObjectMapper objectMapper = new ObjectMapper();
        if (fileStructureStream == null) {
            // Regular file
            File file = new File(location);
            return objectMapper.readValue(file, FileStructureDto.class);
        }
        // Classpath resource
        return objectMapper.readValue(fileStructureStream, FileStructureDto.class);
    }

    /**
     * @return an embedded structure supporting provided file path
     */
    public static Optional<FileStructureDto> retrieveStructureFromSupportedFileName(String filePath) throws IOException {
        String fileName = Paths.get(requireNonNull(filePath, "Supported file path is required")).getFileName().toString();

        // Load all external structures
        List<FileStructureDto> externalStructures = new ArrayList<>();
        Path externalStructuresPath = Paths.get(FileConstants.DIRECTORY_EXTERNAL_STRUCTURES);
        if (Files.exists(externalStructuresPath)) {
            try (Stream<Path> paths = Files.walk(externalStructuresPath)) {
                externalStructures.addAll(paths
                        .filter(Files::isRegularFile)
                        .map(resourcePath -> retrieveStructureFromLocationFailSafe(resourcePath.toString()))
                        .filter(StructureHelper::isStructureEligibleToAuto)
                        .collect(toList()));
            }
        }

        // Load all embedded structures
        List<FileStructureDto> embeddedStructures = FilesHelper.getResourcesFromDirectory("files/structures", "json").parallelStream()
                .map(resourcePath -> retrieveStructureFromLocationFailSafe("/" + resourcePath))
                .filter(StructureHelper::isStructureEligibleToAuto)
                .collect(toList());

        return findFileStructureCandidate(fileName, externalStructures, embeddedStructures);
    }

    /**
     * Encrypts specified output stream according to cryptoMode parameter.
     *
     * @param outputStream : output stream to process, if needed
     * @param cryptoMode   : integer value indicating which encryption mode to use. May be null.
     * @return original output stream if no encryption has been performed, else an encrypted output stream.
     */
    public static ByteArrayOutputStream encryptIfNeeded(ByteArrayOutputStream outputStream, Integer cryptoMode) throws IOException {
        if (cryptoMode == null) {
            return outputStream;
        }

        CryptoHelper.EncryptionModeEnum encryptionModeEnum = CryptoHelper.EncryptionModeEnum.fromIdentifier(cryptoMode);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        return CryptoHelper.encryptXTEA(inputStream, encryptionModeEnum);
    }

    /**
     * Decrypts specified input stream according to cryptoMode parameter.
     *
     * @param inputStream : input stream to process, if needed
     * @param cryptoMode  : integer value indicating which decryption mode to use. May be null.
     * @return original input stream if no encryption has been performed, else an encrypted input stream.
     */
    public static XByteArrayInputStream decryptIfNeeded(XByteArrayInputStream inputStream, Integer cryptoMode) throws IOException {
        if (cryptoMode == null) {
            return inputStream;
        }

        CryptoHelper.EncryptionModeEnum encryptionModeEnum = CryptoHelper.EncryptionModeEnum.fromIdentifier(cryptoMode);

        ByteArrayOutputStream outputStream = CryptoHelper.decryptXTEA(inputStream, encryptionModeEnum);

        return new XByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * @return Field definition from its full name (including parents and indexes).
     */
    public static Optional<FileStructureDto.Field> getFieldDefinitionFromFullName(String fieldName, FileStructureDto fileStructureObject) {
        requireNonNull(fileStructureObject, "File structure object is required");

        if (fieldName == null) {
            return Optional.empty();
        }

        List<String> compounds = Stream.of(fieldName.split("\\."))

                .map(StructureHelper::removeArrayArtefacts)

                .collect(toList());

        return searchFieldWithNameRecursively(compounds, fileStructureObject.getFields());
    }

    private static FileStructureDto retrieveStructureFromLocationFailSafe(String location) {
        try {
            return retrieveStructureFromLocation(location);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String removeArrayArtefacts(String compoundName) {
        int arrayArtefactIndex = compoundName.indexOf('[');

        if (arrayArtefactIndex == -1) {
            return compoundName;
        }

        return compoundName.substring(0, arrayArtefactIndex);
    }

    private static Optional<FileStructureDto.Field> searchFieldWithNameRecursively(List<String> compounds, List<FileStructureDto.Field> subFields) {
        requireNonNull(subFields, "A list of sub fields is required");

        for (FileStructureDto.Field field : subFields) {
            if (field.getName().equals(compounds.get(0))) {

                compounds.remove(0);

                if (compounds.isEmpty()) {
                    return Optional.of(field);
                }

                return searchFieldWithNameRecursively(compounds, field.getSubFields());
            }
        }

        return Optional.empty();
    }

    private static Optional<FileStructureDto> findFileStructureCandidate(String fileName, List<FileStructureDto> externalStructures, List<FileStructureDto> embeddedStructures) {
        // For each structure, find the most appropriate to decode provided filePath, relying on file name pattern
        // Priority: external > embedded.

        String logMessageFormat = "Found %s structure candaidate for %s: %s";

        Optional<FileStructureDto> externalCandidate = findFileStructureCandidate(fileName, externalStructures);
        if (externalCandidate.isPresent()) {
            Log.debug(THIS_CLASS_NAME, String.format(logMessageFormat, "external", fileName, externalCandidate.get().getName()));
            return externalCandidate;
        }

        Optional<FileStructureDto> embeddedCandidate = findFileStructureCandidate(fileName, embeddedStructures);
        if (embeddedCandidate.isPresent()) {
            Log.debug(THIS_CLASS_NAME, String.format(logMessageFormat, "embedded", fileName, embeddedCandidate.get().getName()));
            return embeddedCandidate;
        }

        return Optional.empty();
    }

    private static Optional<FileStructureDto> findFileStructureCandidate(String fileName, List<FileStructureDto> structures) {
        return structures.parallelStream()
                .filter(structure -> {
                    if (structure.getFileNamePattern() == null) {
                        return false;
                    }

                    String regexPattern = RegexHelper.createRegexFromGlob(structure.getFileNamePattern());
                    return fileName.toLowerCase().matches(regexPattern);
                })
                .findAny();
    }

    private static boolean isStructureEligibleToAuto(FileStructureDto fileStructureDto) {
        return fileStructureDto != null && fileStructureDto.getFileNamePattern() != null;
    }
}