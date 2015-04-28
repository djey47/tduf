package fr.tduf.libunlimited.high.files.db.interop;

import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

/**
 * Utility class allowing to convert database patches between different formats (TDUF, TDUMT)
 */
public class PatchConverter {

    private static Class<PatchConverter> thisClass = PatchConverter.class;

    /**
     * Convertit un patch TDUF en patch TDUMT (XML).
     * @param tdufDatabasePatch : TDUF patch object to convert
     * @return corresponding TDUMT patch as XML document.
     */
    public static Document jsonToPch(DbPatchDto tdufDatabasePatch) throws ParserConfigurationException, URISyntaxException, IOException, SAXException {
        requireNonNull(tdufDatabasePatch, "A TDUF database patch object is required.");

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        String templateURI = thisClass.getResource("/files/db/tdumt/patchTemplate.xml").toURI().toString();
        Document patchDocument = docBuilder.parse(templateURI);

        return patchDocument;
    }
}