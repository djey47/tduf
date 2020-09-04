package fr.tduf.libunlimited.low.files.gfx.materials.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.*;

import static fr.tduf.libunlimited.common.game.helper.GameEngineHelper.normalizeString;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_COLORS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.INTERIOR;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;

/**
 * Provides utility methods to handle TDU materials
 */
public class MaterialsHelper {

    private static final Set<DbDto.Topic> MATERIAL_TOPICS = new HashSet<>(asList(CAR_COLORS, INTERIOR));

    /**
     * @param miner - database miner
     * @return a map, with key = normalized material name, value = database material name
     */
    public static Map<String, String> buildNormalizedDictionary(BulkDatabaseMiner miner) {
        final Map<String, String> completeDic = new HashMap<>();

        MATERIAL_TOPICS.forEach(topic -> {
            Map<String, String> topicDic = miner.getResourcesFromTopic(topic)
                    .orElseThrow(() -> new IllegalStateException("No resources for topic " + topic))
                    .getEntries().parallelStream()
                    .map(resourceEntryDto -> resourceEntryDto.pickValue().orElseThrow(() -> new IllegalStateException("")))
                    .collect(toMap(
                            value -> normalizeString(value.toUpperCase()),
                            value -> value,
                            (v1, v2) -> v1));
            completeDic.putAll(topicDic);
        });

        return completeDic;
    }
}