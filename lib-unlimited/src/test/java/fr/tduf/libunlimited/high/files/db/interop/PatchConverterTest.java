package fr.tduf.libunlimited.high.files.db.interop;


import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class PatchConverterTest {

    @Test(expected = NullPointerException.class)
    public void jsonToPch_whenNullObject_shouldThrowException() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN-WHEN
        PatchConverter.jsonToPch(null);

        // THEN: NPE
    }

    @Test
    public void jsonToPch_whenNoChange_shouldReturnDefaultPatch() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        DbPatchDto patchObject = DbPatchDto.builder().build();


        // WHEN
        Document actualDocument = PatchConverter.jsonToPch(patchObject);


        // THEN
        assertThat(actualDocument).isNotNull();

        NodeList propertiesNodes = actualDocument.getDocumentElement().getElementsByTagName("properties");
        assertThat(propertiesNodes.getLength()).isEqualTo(1);

        Element propertiesNode = (Element) propertiesNodes.item(0);
        assertThat(propertiesNode.getAttributes().getLength()).isEqualTo(8);
        assertThat(propertiesNode.getElementsByTagName("roles").getLength()).isEqualTo(1);

        NodeList instructionsNodes = actualDocument.getDocumentElement().getElementsByTagName("instructions");
        assertThat(instructionsNodes.getLength()).isEqualTo(1);

        Element instructionNode = (Element) instructionsNodes.item(0);
        assertThat(instructionNode.getChildNodes().getLength()).isZero();
    }

    @Test
    public void jsonToPch_whenRealPatchObject_shouldReturnPatch() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        DbPatchDto patchObject = FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-ref.mini.json");


        // WHEN
        Document actualDocument = PatchConverter.jsonToPch(patchObject);


        // THEN
        assertThat(actualDocument).isNotNull();

        NodeList instructions = assertStructureAndReturnInstructions(actualDocument, 2);

        Element instruction1 = (Element) instructions.item(0);
        assertUpdateDatabaseInstruction(instruction1, "CarPhysicsData", "1221657049|1221657049\t864426\t56338407\t");

        Element instruction2 = (Element) instructions.item(1);
        assertUpdateDatabaseInstruction(instruction2, "CarPhysicsData", "606298799|606298799\t864426\t56338407\t");
    }

    @Test
    public void jsonToPch_whenRealPatchObject_ForResources_shouldReturnPatch() throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        // GIVEN
        DbPatchDto patchObject = FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateResources.mini.json");


        // WHEN
        Document actualDocument = PatchConverter.jsonToPch(patchObject);


        // THEN
        assertThat(actualDocument).isNotNull();

        NodeList instructions = assertStructureAndReturnInstructions(actualDocument, 2);

        Element instruction1 = (Element) instructions.item(0);
        assertUpdateResourceInstruction(instruction1, "Bots", "54367256|Brian Molko");

        Element instruction2 = (Element) instructions.item(1);
        assertUpdateResourceInstruction(instruction2, "Bots", "33333333|Cindy");
    }

    private static NodeList assertStructureAndReturnInstructions(Document actualDocument, int instructionsCount) {
        NodeList instructionsNodes = actualDocument.getDocumentElement().getElementsByTagName("instructions");
        assertThat(instructionsNodes.getLength()).isEqualTo(1);

        Element instructionsNode = (Element) instructionsNodes.item(0);
        NodeList instructions = instructionsNode.getElementsByTagName("instruction");

        assertThat(instructions.getLength()).isEqualTo(instructionsCount);

        return instructions;
    }

    private static void assertUpdateDatabaseInstruction(Element instruction, String resourceName, String beginningOfResourceValue) {
        assertThat(instruction.getAttribute("type")).isEqualTo("updateDatabase");

        Element resourceNameParameter = (Element) instruction.getElementsByTagName("parameter").item(0);
        Element resourceValueParameter = (Element) instruction.getElementsByTagName("parameter").item(1);

        assertThat(resourceNameParameter.getAttribute("value")).isEqualTo(resourceName);
        assertThat(resourceValueParameter.getAttribute("value")).startsWith(beginningOfResourceValue);
    }

    private static void assertUpdateResourceInstruction(Element instruction, String resourceName, String resourceValue) {
        assertThat(instruction.getAttribute("type")).isEqualTo("updateResource");

        Element resourceNameParameter = (Element) instruction.getElementsByTagName("parameter").item(0);
        Element resourceValueParameter = (Element) instruction.getElementsByTagName("parameter").item(1);

        assertThat(resourceNameParameter.getAttribute("value")).isEqualTo(resourceName);
        assertThat(resourceValueParameter.getAttribute("value")).isEqualTo(resourceValue);
    }
}