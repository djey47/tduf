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
            Log.error(THIS_CLASS_NAME, "Unable to load database metadata resources", e);
        }
    }

    private static void loadDatabaseReference() throws IOException, URISyntaxException {
        databaseMetadataObject = new DbMetadataDto();
        loadTopicsReference();
        loadDealersReference();
        loadIKReference();
        loadCameraReference();
    }

    private static void loadTopicsReference() throws IOException, URISyntaxException {
        DbMetadataDto.AllTopicsMetadataDto allTopicsMetadataDto = FilesHelper.readObjectFromJsonResourceFile(DbMetadataDto.AllTopicsMetadataDto.class, "/files/db/metadata/topics.json");
        databaseMetadataObject.setTopics(allTopicsMetadataDto.getTopics());
    }

    private static void loadDealersReference() throws IOException, URISyntaxException {
        DbMetadataDto.AllDealersMetadataDto allDealersMetadataDto = FilesHelper.readObjectFromJsonResourceFile(DbMetadataDto.AllDealersMetadataDto.class, "/files/db/metadata/dealers.json");
        databaseMetadataObject.setDealers(allDealersMetadataDto.getDealers());
    }

    private static void loadIKReference() throws IOException, URISyntaxException {
        //noinspection unchecked
        Map<String, String> iks = FilesHelper.readObjectFromJsonResourceFile(Map.class, "/files/db/metadata/iks.json");
        databaseMetadataObject.setIKs(
                iks.entrySet().stream()
                        .collect(toMap(e -> Integer.valueOf(e.getKey()), Map.Entry::getValue)));
    }

    private static void loadCameraReference() throws IOException, URISyntaxException {
        //noinspection unchecked
        Map<String, String> cameras = FilesHelper.readObjectFromJsonResourceFile(Map.class, "/files/db/metadata/cameras.json");
        databaseMetadataObject.setCameras(
                cameras.entrySet().stream()
                        .collect(toMap(e -> Long.valueOf(e.getKey()), Map.Entry::getValue)));
    }
}
