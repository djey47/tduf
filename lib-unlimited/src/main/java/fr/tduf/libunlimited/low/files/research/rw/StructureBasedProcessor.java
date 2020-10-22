package fr.tduf.libunlimited.low.files.research.rw;

import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.IOException;

/**
 * Describes contract to use a filesystem or classpath resource as a structure for processing objects.
 */
public interface StructureBasedProcessor {

    /**
     * Can be used: either resource in classpath, or file path.
     * @return location of resource used to describe parsed file structure (optional).
     */
    String getStructureResource();

    /**
     * Used as fallback when structure resource has not been provided
     * @return structure describing parsed file (mandatory when structure resource not provided)
     */
    FileStructureDto getStructure();
}