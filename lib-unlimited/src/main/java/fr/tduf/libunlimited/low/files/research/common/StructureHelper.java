package fr.tduf.libunlimited.low.files.research.common;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
}