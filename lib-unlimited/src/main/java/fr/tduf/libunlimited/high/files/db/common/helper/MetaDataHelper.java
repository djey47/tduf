package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Parent class for all helpers based on metadata resource
 */
public abstract class MetaDataHelper {

    protected DbMetadataDto databaseMetadataObject;

    protected MetaDataHelper() {
        try {
            loadDatabaseReference();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void loadDatabaseReference() throws IOException, URISyntaxException {
        databaseMetadataObject = FilesHelper.readObjectFromJsonResourceFile(DbMetadataDto.class, "/files/db/databaseMetadata.json");
    }
}
