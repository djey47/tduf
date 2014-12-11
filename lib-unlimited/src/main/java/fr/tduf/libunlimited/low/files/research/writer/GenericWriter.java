package fr.tduf.libunlimited.low.files.research.writer;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * Helper to write files whose file structure is available as separate asset.
 */
public abstract class GenericWriter<T> {

    private static Class<GenericWriter> thisClass = GenericWriter.class;

    private final FileStructureDto fileStructure;

    private final DataStore dataStore = new DataStore();

    private final T data;

    protected GenericWriter(T data) throws IOException {
        requireNonNull(data, "Data is required");
        requireNonNull(getStructureResource(), "Data structure resource is required");

        InputStream fileStructureStream = thisClass.getResourceAsStream(getStructureResource());

        this.data = data;
        this.fileStructure = new ObjectMapper().readValue(fileStructureStream, FileStructureDto.class);
    }

    /**
     * Writes current data object to byte stream.
     * @return a stream with serialized data according to provided structure
     */
    public ByteArrayOutputStream write() {
        return null;
    }

    /**
     * @return location of resource used to describe parsed file structure (mandatory).
     */
    protected abstract String getStructureResource();

    FileStructureDto getFileStructure() {
        return fileStructure;
    }

    T getData() {
        return data;
    }
}