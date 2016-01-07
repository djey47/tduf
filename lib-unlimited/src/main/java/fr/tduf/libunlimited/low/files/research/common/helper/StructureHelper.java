package fr.tduf.libunlimited.low.files.research.common.helper;

import fr.tduf.libunlimited.low.files.common.crypto.helper.CryptoHelper;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Utility class to provide common operations on Structures.
 */
public class StructureHelper {

    /**
     * @param resource  : resource name of file location.
     * @return file structure, according to specified location
     */
    public static FileStructureDto retrieveStructureFromLocation(String resource) throws IOException {
        InputStream fileStructureStream = StructureHelper.class.getResourceAsStream(resource);
        if (fileStructureStream == null) {
            // Regular file
            File file = new File(resource);
            return new ObjectMapper().readValue(file, FileStructureDto.class);
        }
        // Classpath resource
        return new ObjectMapper().readValue(fileStructureStream, FileStructureDto.class);
    }

    /**
     * Encrypts specified output stream according to cryptoMode parameter.
     *
     * @param outputStream  : output stream to process, if needed
     * @param cryptoMode    : integer value indicating which encryption mode to use. May be null.
     * @return original output stream if no encryption has been performed, else an encrypted output stream.
     */
    public static ByteArrayOutputStream encryptIfNeeded (ByteArrayOutputStream outputStream, Integer cryptoMode) throws IOException {
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
     * @param inputStream  : input stream to process, if needed
     * @param cryptoMode    : integer value indicating which decryption mode to use. May be null.
     * @return original input stream if no encryption has been performed, else an encrypted input stream.
     */
    public static ByteArrayInputStream decryptIfNeeded (ByteArrayInputStream inputStream, Integer cryptoMode) throws IOException {
        if (cryptoMode == null) {
            return inputStream;
        }

        CryptoHelper.EncryptionModeEnum encryptionModeEnum = CryptoHelper.EncryptionModeEnum.fromIdentifier(cryptoMode);

        ByteArrayOutputStream outputStream = CryptoHelper.decryptXTEA(inputStream, encryptionModeEnum);

        return new ByteArrayInputStream(outputStream.toByteArray());
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
}