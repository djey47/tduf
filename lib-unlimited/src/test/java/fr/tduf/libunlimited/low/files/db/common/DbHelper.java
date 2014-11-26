package fr.tduf.libunlimited.low.files.db.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Helper class to process TDU database.
 */
public class DbHelper {

    private static Class<DbHelper> thisClass = DbHelper.class;

    /**
     * Reads provided resource text files and return contents as lines.
     * Eligible files are TDU_*.fr ... etc, having UTF-16LE as encoding.
     * @param sampleFiles names of files to read
     * @return a list of list of lines
     * @throws IOException
     */
    public static List<List<String>> readResourcesFromSamples(String... sampleFiles) throws IOException {
        List<List<String>> resourceLines = newArrayList();

        for (String sampleFile : sampleFiles) {
            resourceLines.add(readContentsFromSample(sampleFile, "UTF-16"));
        }

        return resourceLines;
    }

    /**
     * Reads provided text file and return contents as lines.
     * Eligible files have CRLF as line delimiter
     * @param sampleFile names of file to read
     * @return a list of lines
     * @throws IOException
     */
    public static List<String> readContentsFromSample(String sampleFile, String encoding) throws IOException {
        List<String> lines = newArrayList();

        InputStream resourceAsStream = thisClass.getResourceAsStream(sampleFile);

        Scanner scanner = new Scanner(resourceAsStream, encoding) ;
        scanner.useDelimiter("\r\n");

        while(scanner.hasNext()) {
            lines.add(scanner.next());
        }

        return lines;
    }

    /**
     * Reads provided text file and return contents.
     * @param sampleFile name of file to read
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
}