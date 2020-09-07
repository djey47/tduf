package fr.tduf.libunlimited.low.files.gfx.materials.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import fr.tduf.libunlimited.low.files.gfx.materials.rw.MaterialsWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.common.game.helper.GameEngineHelper.normalizeString;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_COLORS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.INTERIOR;
import static fr.tduf.libunlimited.low.files.gfx.materials.helper.StoreConstants.FIELD_SUFFIXES_COLORS;
import static fr.tduf.libunlimited.low.files.gfx.materials.helper.StoreConstants.FORMAT_COLOR_SETTINGS_KEY;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

/**
 * Provides utility methods to handle TDU materials
 */
public class MaterialsHelper {

    private static final Set<DbDto.Topic> MATERIAL_TOPICS = new HashSet<>(asList(CAR_COLORS, INTERIOR));

    /**
     * @param miner : database miner
     * @return a map, with key = normalized material name, value = database material name
     */
    public static Map<String, String> buildNormalizedDictionary(BulkDatabaseMiner miner) {
        final Map<String, String> completeDic = new HashMap<>();
        updateNormalizedDictionary(completeDic, miner);
        return completeDic;
    }

    /**
     * Updates dictionary
     * @param dicToUpdate   : dictionary instance
     * @param miner         : database miner
     */
    public static void updateNormalizedDictionary(Map<String, String> dicToUpdate, BulkDatabaseMiner miner) {
        MATERIAL_TOPICS.forEach(topic -> {
            Map<String, String> topicDic = getAllResourcesAsParallelStream(topic, miner)
                    .map(MaterialsHelper::pickResourceValue)
                    .collect(toMap(
                            value -> normalizeString(value.toUpperCase()),
                            value -> value,
                            (v1, v2) -> v1));
            dicToUpdate.putAll(topicDic);
        });
    }

    /**
     * Write file according to .2DM file format
     * @param materialDefs    : parsed material definitions with eventual changes
     * @param materialsFile   : file to be written. Existing file will be replaced.
     * @throws IOException when a file system error occurs
     */
    public static void saveMaterialDefinitions(MaterialDefs materialDefs, String materialsFile) throws IOException {
        ByteArrayOutputStream outputStream = MaterialsWriter.load(materialDefs).write();
        Files.write(Paths.get(materialsFile), outputStream.toByteArray());
    }

    /**
     * @param groupKeyName      : name of group containing color settings keys
     * @param settingKeyName    : name of color setting key without compound suffix
     * @return a stream of key names, allowing access to data store info
     */
    public static Stream<String> getColorSettingsKeyStream(String groupKeyName, String settingKeyName) {
        return IntStream.range(0, 4)
                .mapToObj(itemIndex -> {
                    String currentSuffix = FIELD_SUFFIXES_COLORS[itemIndex];
                    return String.format(FORMAT_COLOR_SETTINGS_KEY, groupKeyName, settingKeyName, currentSuffix);
                });
    }

    /**
     * @param materialName  : material name to provide a REF for
     * @param topic         : enclosing database topic
     * @param miner         : database miner, to perform lookup ops
     * @return existing resource reference for given material name
     */
    public static String getResourceRefForMaterialName(String materialName, DbDto.Topic topic, BulkDatabaseMiner miner) {
        return getAllResourcesAsParallelStream(topic, miner)
                .filter(resourceEntry -> pickResourceValue(resourceEntry).equalsIgnoreCase(materialName))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("No resource entry for material name " + materialName))
                .getReference();
    }

    /**
     * @param materialName  : material name to be searched
     * @param topic         : enclosing database topic
     * @param miner         : database miner, to perform lookup ops
     * @return true if such a material name exists in resources, false otherwise
     */
    public static boolean isExistingMaterialNameInResources(String materialName, DbDto.Topic topic, BulkDatabaseMiner miner) {
        return getAllResourcesAsParallelStream(topic, miner)
                .map(MaterialsHelper::pickResourceValue)
                .anyMatch(value -> value.equalsIgnoreCase(materialName));
    }

    private static String pickResourceValue(ResourceEntryDto resourceEntry) {
        return resourceEntry.pickValue()
                .orElseThrow(() -> new IllegalStateException("No resource value available"));
    }

    private static Stream<ResourceEntryDto> getAllResourcesAsParallelStream(DbDto.Topic topic, BulkDatabaseMiner miner) {
        return miner.getResourcesFromTopic(topic)
                .orElseThrow(() -> new IllegalStateException("No resources for topic: " + topic.name()))
                .getEntries().parallelStream();
    }
}
