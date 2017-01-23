package fr.tduf.libunlimited.high.files.db.common.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Parent class for all helpers based on metadata resource
 */
abstract class MetaDataHelper {
    private static final String THIS_CLASS_NAME = MetaDataHelper.class.getSimpleName();

    protected static DbMetadataDto databaseMetadataObject;

    static {
        try {
            loadDatabaseReference();
        } catch (IOException | URISyntaxException e) {
            Log.error(THIS_CLASS_NAME, "Unable to load database metadata resource", e);
        }
    }

    private static void loadDatabaseReference() throws IOException, URISyntaxException {
        // TODO split into resource files
        databaseMetadataObject = FilesHelper.readObjectFromJsonResourceFile(DbMetadataDto.class, "/files/db/databaseMetadata.json");

        loadIKReference();
    }

    private static void loadIKReference() throws IOException, URISyntaxException {
        //noinspection unchecked
        Map<String, String> iks = FilesHelper.readObjectFromJsonResourceFile(Map.class, "/files/db/metadata/iks.json");
        databaseMetadataObject.setIKs(
                iks.entrySet().stream()
                        .collect(toMap(e -> Integer.valueOf(e.getKey()), Map.Entry::getValue)));
    }
}
