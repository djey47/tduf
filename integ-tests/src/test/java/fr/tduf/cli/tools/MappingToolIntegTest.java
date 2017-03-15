package fr.tduf.cli.tools;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapParser;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.tduf.tests.IntegTestsConstants.RESOURCES_PATH;
import static org.assertj.core.api.Assertions.assertThat;

public class MappingToolIntegTest {

    private final String mappingDirectory = RESOURCES_PATH.resolve("mapping").toString();
    private final String contentsDirectory1 = Paths.get(mappingDirectory).resolve("cnt1").toString();
    private final String contentsDirectory2 = Paths.get(contentsDirectory1).resolve("cnt2").toString();

    @BeforeEach
    void setUp() throws IOException {
        FileUtils.deleteDirectory(new File(mappingDirectory));
        FilesHelper.createDirectoryIfNotExists(contentsDirectory2);
    }

    @Test
    void info_ListMissing_FixMissing_List_whenEmptyInitialMap_andAddingNewFiles_shouldNotThrowError() throws IOException {
        //GIVEN: empty map file
        BankMap bankMap = createEmptyMap();
        bankMap.addMagicEntry(0);

        String emptyMapFilePath = createMapFile(bankMap, mappingDirectory);


        //WHEN: info
        System.out.println("-> Info!");
        MappingTool.main(new String[] { "info", "-b", mappingDirectory });


        //GIVEN: 2 new files to bank directory
        FilesHelper.createFileIfNotExists(Paths.get(contentsDirectory1, "c1").toString());
        FilesHelper.createFileIfNotExists(Paths.get(contentsDirectory2, "c2").toString());


        //WHEN: list-missing
        System.out.println("-> List-missing!");
        MappingTool.main(new String[]{"list-missing", "-n", "-b", mappingDirectory, "-m", emptyMapFilePath});


        //WHEN: fix-missing
        System.out.println("-> Fix-missing!");
        MappingTool.main(new String[]{"fix-missing", "-n", "-b", mappingDirectory});


        //WHEN: list
        System.out.println("-> List!");
        MappingTool.main(new String[]{"list", "-b", mappingDirectory});
    }

    @Test
    void magify_whenMixedEntriesInProvidedMap_shouldConvertToMagicMap() throws IOException {
        // GIVEN
        BankMap bankMap = createEmptyMap();
        bankMap.addEntry(1589L, 1L, 1L);
        bankMap.addEntry(1590L, 2L, 2L);
        bankMap.addMagicEntry(1591L);

        String mapFileName = createMapFile(bankMap, mappingDirectory);


        // WHEN: magify
        System.out.println("-> magify!");
        MappingTool.main(new String[]{"magify", "-n", "-m", mapFileName});


        // THEN
        BankMap actualBankMap = MapParser.load(mapFileName).parse();
        assertThat(actualBankMap.getEntries()).hasSize(3);
        assertThat(actualBankMap.isMagic()).isTrue();
    }

    private static BankMap createEmptyMap() {
        BankMap bankMap = new BankMap();
        bankMap.setEntrySeparator(new byte[] { (byte)0xFE, 0x12, 0x00, 0x00});
        bankMap.setTag("MAP4");

        return bankMap;
    }

    private static String createMapFile(BankMap bankMap, String mappingDirectory) throws IOException {
        ByteArrayOutputStream mapOutputStream = MapWriter.load(bankMap).write();

        File mapFileName = new File(mappingDirectory, "Bnk1.map");
        String mapAbsolutePath = mapFileName.getAbsolutePath();
        Files.write(Paths.get(mapAbsolutePath), mapOutputStream.toByteArray());

        return mapAbsolutePath;
    }
}
