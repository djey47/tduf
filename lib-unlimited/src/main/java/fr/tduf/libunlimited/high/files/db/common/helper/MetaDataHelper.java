package fr.tduf.libunlimited.high.files.db.common.helper;

import com.esotericsoftware.minlog.Log;
import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Parent class for all helpers based on metadata resource
 */
public abstract class MetaDataHelper {
    private static final String THIS_CLASS_NAME = MetaDataHelper.class.getSimpleName();

    protected DbMetadataDto databaseMetadataObject;

    protected MetaDataHelper() {
        try {
            loadDatabaseReference();
        } catch (IOException | URISyntaxException e) {
            Log.error(THIS_CLASS_NAME, "Unable to load database metadata resource", e);
        }
    }

    private void loadDatabaseReference() throws IOException, URISyntaxException {
        databaseMetadataObject = FilesHelper.readObjectFromJsonResourceFile(DbMetadataDto.class, "/files/db/databaseMetadata.json");
    }
}
