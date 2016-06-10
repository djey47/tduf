package fr.tduf.libunlimited.low.files.db.common.helper;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.rw.helper.DatabaseReadWriteHelper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DbConverterHelper {

    @Test
    public void splitJsonFiles() {

        String dir = "/opt/workspaces/perso-git/tduf/cli/integ-tests/db-json-diff";

        final List<DbDto> dbdtos = DbDto.Topic.valuesAsStream()
                .map(topic -> {
                    try {
                        return DatabaseReadWriteHelper.readGenuineDatabaseTopicFromJson(topic, dir)
                                .orElse(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(o -> o != null)
                .collect(Collectors.toList());

        DatabaseReadWriteHelper.writeDatabaseTopicsToJson(dbdtos, dir);
    }
}
