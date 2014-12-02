package fr.tduf.libunlimited.low.files.db;


import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseReadWriteHelperTest {

    private static Class<DatabaseReadWriteHelperTest> thisClass = DatabaseReadWriteHelperTest.class;

    @Test
    public void parseTopicContentsFromDirectory_whenRealFile_shouldReturnContentsAsLines() throws URISyntaxException, FileNotFoundException {
        // GIVEN
        File dbFile = new File(thisClass.getResource("/db/TDU_Achievements.db").toURI());
        String databaseDirectory = dbFile.getParent();

        // WHEN
        List<String> actualContentLines = DatabaseReadWriteHelper.parseTopicContentsFromDirectory(DbDto.Topic.ACHIEVEMENTS, databaseDirectory);

        // THEN
        assertThat(actualContentLines).isNotNull();
        assertThat(actualContentLines).hasSize(90);
    }

    @Test
    public void parseTopicResourcesFromDirectory_whenRealFiles_shouldReturnContentsAsCollections() throws URISyntaxException, FileNotFoundException {
        // GIVEN
        File dbFile = new File(thisClass.getResource("/db/res/TDU_Achievements.fr").toURI());
        String databaseDirectory = dbFile.getParent();

        // WHEN
        List<List<String>> allResources = DatabaseReadWriteHelper.parseTopicResourcesFromDirectory(DbDto.Topic.ACHIEVEMENTS, databaseDirectory);

        // THEN
        assertThat(allResources).isNotNull();
        assertThat(allResources).hasSize(8);
        assertThat(allResources.get(0)).hasSize(253); //fr
        assertThat(allResources.get(1)).hasSize(0);
        assertThat(allResources.get(2)).hasSize(0);
        assertThat(allResources.get(3)).hasSize(0);
        assertThat(allResources.get(4)).hasSize(0);
        assertThat(allResources.get(5)).hasSize(0);
        assertThat(allResources.get(6)).hasSize(253); //it
        assertThat(allResources.get(7)).hasSize(0);
    }
}