package fr.tduf.cli.tools;

import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.low.files.banks.mapping.domain.BankMap;
import fr.tduf.libunlimited.low.files.banks.mapping.rw.MapWriter;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MappingToolIntegTest {

    private final String mappingDirectory = "integ-tests/mapping";
    private final String contentsDirectory1 = "integ-tests/mapping/cnt1";
    private final String contentsDirectory2 = "integ-tests/mapping/cnt1/cnt2";

    @Before
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(new File(mappingDirectory));
        FilesHelper.createDirectoryIfNotExists(contentsDirectory2);
    }

    @Test
    public void info_ListMissing_FixMissing_List_whenEmptyInitialMap_andAddingNewFiles_shouldNotThrowError() throws IOException {
        //GIVEN: empty map file
        String emptyMapFilePath = createEmptyMapFile(mappingDirectory);

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

    private static String createEmptyMapFile(String mappingDirectory) throws IOException {
        BankMap bankMap = new BankMap();
        bankMap.setEntrySeparator(new byte[] { (byte)0xFE, 0x12, 0x00, 0x00});
        bankMap.setTag("MAP4");
        bankMap.addMagicEntry(0);

        ByteArrayOutputStream mapOutputStream = MapWriter.load(bankMap).write();

        File mapFileName = new File(mappingDirectory, "Bnk1.map");
        String mapAbsolutePath = mapFileName.getAbsolutePath();
        Files.write(Paths.get(mapAbsolutePath), mapOutputStream.toByteArray());

        return mapAbsolutePath;
    }
}