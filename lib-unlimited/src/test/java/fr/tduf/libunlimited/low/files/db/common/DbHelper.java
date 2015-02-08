package fr.tduf.libunlimited.low.files.db.common;

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
 * Helper class to process TDU database.
 */
// TODO Javadoc
public class DbHelper {

    private static Class<DbHelper> thisClass = DbHelper.class;

    /**
     * Reads provided resource text files and return contents as lines.
     * Eligible files are TDU_*.fr ... etc, having UTF-16LE as encoding.
     * @param sampleFiles resource names of files to read
     * @return a list of list of lines
     * @throws IOException
     */
    public static List<List<String>> readResourcesFromSamples(String... sampleFiles) throws IOException {
        List<List<String>> resourceLines = new ArrayList<>();

        for (String sampleFile : sampleFiles) {
            resourceLines.add(readContentsFromSample(sampleFile, "UTF-16", "\r\n"));
        }

        return resourceLines;
    }

    /**
     * Reads provided text file and return contents as lines.
     * Eligible files have CRLF as line delimiter
     * @param sampleFile resource name of file to read
     * @return a list of lines
     * @throws IOException
     */
    public static List<String> readContentsFromSample(String sampleFile, String encoding, String lineDelimiter) throws IOException {

        InputStream resourceAsStream = thisClass.getResourceAsStream(sampleFile);

        return readContentsFromStream(resourceAsStream, encoding, lineDelimiter);
    }

    /**
     * Reads provided text file and return contents.
     * @param sampleFile resource name of file to read
     * @param charsetName name of character set used in provided file
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
     *
     * @param fileName
     * @param encoding
     * @return
     */
    public static List<String> readContentsFromRealFile(String fileName, String encoding, String lineDelimiter) throws FileNotFoundException {

        InputStream inputStream = new FileInputStream(fileName);

        return readContentsFromStream(inputStream, encoding, lineDelimiter);
    }

    /**
     *
     * @param fileNames
     * @return
     */
    public static List<List<String>> readResourcesFromRealFiles(String... fileNames) throws FileNotFoundException {
        List<List<String>> resourceLines = new ArrayList<>();

        for (String fileName : fileNames) {
            resourceLines.add(readContentsFromRealFile(fileName, "UTF-16", "\r\n"));
        }

        return resourceLines;
    }

    private static List<String> readContentsFromStream(InputStream inputStream, String encoding, String lineDelimiter) {
        List<String> lines = new ArrayList<>();

        Scanner scanner = new Scanner(inputStream, encoding) ;
        scanner.useDelimiter(lineDelimiter);

        while(scanner.hasNext()) {
            lines.add(scanner.next());
        }

        return lines;
    }
}