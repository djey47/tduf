package fr.tduf.libunlimited.low.files.db.common.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helps with data migration
 */
@Ignore
public class DbConverterHelper {

    /**
     * Converts to 3 JSON files per topic instead of single one
     */
    @Test
    public void splitJsonFiles() {

        String jsonDirectory = "/opt/workspaces/perso-git/tduf/lib-unlimited/src/test/resources/db/json";

        final List<DbDto> dbdtos = DbDto.Topic.valuesAsStream()
                .map(topic -> {
                    try {
                        return DatabaseReadWriteHelper.readGenuineDatabaseTopicFromJson(topic, jsonDirectory)
                                .orElse(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(o -> o != null)
                .collect(Collectors.toList());

        DatabaseReadWriteHelper.writeDatabaseTopicsToJson(dbdtos, jsonDirectory);
    }
}
