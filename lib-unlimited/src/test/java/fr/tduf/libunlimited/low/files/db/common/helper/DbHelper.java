package fr.tduf.libunlimited.low.files.db.common.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Helper class to process TDU database during tests.
 */
public class DbHelper {

    private static Class<DbHelper> thisClass = DbHelper.class;

    /**
     * Reads provided resource text files and return contents as lines.
     * Eligible files are TDU_*.fr ... etc, having UTF-16LE as encoding.
     * @param sampleFiles   : resource names of files to read
     * @return a list of list of lines
     * @throws IOException
     */
    public static List<List<String>> readResourcesFromSamples(String... sampleFiles) throws IOException {
        List<List<String>> resourceLines = new ArrayList<>();

        for (String sampleFile : sampleFiles) {
            resourceLines.add(readContentsFromSample(sampleFile, "UTF-16"));
        }

        return resourceLines;
    }

    /**
     * Reads provided text file and return contents as lines.
     * Eligible files have CRLF as line delimiter
     * @param sampleFile    : resource name of file to read
     * @param encoding      : etither UTF-8 or UTF-16
     * @return a list of lines
     * @throws IOException
     */
    public static List<String> readContentsFromSample(String sampleFile, String encoding) throws IOException {

        InputStream resourceAsStream = thisClass.getResourceAsStream(sampleFile);

        return readContentsFromStream(resourceAsStream, encoding);
    }

    /**
     * Reads provided text file and return contents.
     * @param sampleFile    : resource name of file to read
     * @param charsetName   : name of character set used in provided file
     * @return a String containing all text in file
     * @throws IOException
     * @throws URISyntaxException
     */
    public static String readTextFromSample(String sampleFile, String charsetName) throws IOException, URISyntaxException {
        Path path = Paths.get(thisClass.getResource(sampleFile).toURI());
        byte[] encoded = Files.readAllBytes(path);

        return new String(encoded, Charset.forName(charsetName));
    }

    /**
     * Reads provided text files and return contents as lists of lines.
     * @param fileNames : names of ile to be processed
     */
    public static List<List<String>> readResourcesFromRealFiles(String... fileNames) throws FileNotFoundException {
        List<List<String>> resourceLines = new ArrayList<>();

        for (String fileName : fileNames) {
            resourceLines.add(readContentsFromRealFile(fileName, "UTF-16"));
        }

        return resourceLines;
    }

    /**
     * Extracts all lines from database files.
     * @param fileName      : unencrypted, database contents file (e.g TDU_Achievements.fr)
     * @param encoding      : either UTF-8 or UTF-16
     */
    public static List<String> readContentsFromRealFile(String fileName, String encoding) throws FileNotFoundException {

        InputStream inputStream = new FileInputStream(fileName);

        return readContentsFromStream(inputStream, encoding);
    }

    private static List<String> readContentsFromStream(InputStream inputStream, String encoding) {
        List<String> lines = new ArrayList<>();

        Scanner scanner = new Scanner(inputStream, encoding) ;
        scanner.useDelimiter("\r\n");

        while(scanner.hasNext()) {
            lines.add(scanner.next());
        }

        return lines;
    }
}