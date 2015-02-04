package fr.tduf.libunlimited.low.files.research.common;

import fr.tduf.libunlimited.low.files.common.crypto.CryptoHelper;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;

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
}