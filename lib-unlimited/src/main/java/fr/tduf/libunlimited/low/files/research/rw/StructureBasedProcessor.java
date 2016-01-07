package fr.tduf.libunlimited.low.files.research.rw;

/**
 * Describes contract to use a filesystem or classpath resource as a structure for processing objects.
 */
public interface StructureBasedProcessor {

    /**
     * Can be used: either resource in classpath, or file path.
     * @return location of resource used to describe parsed file structure (mandatory).
     */
    String getStructureResource();
}