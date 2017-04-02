package fr.tduf.libunlimited.high.files.db.interop;


import fr.tduf.libunlimited.common.helper.FilesHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.DELETE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE;
import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.UPDATE_RES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TdumtPatchConverterTest {

    @Test
    void jsonToPch_whenNullObject_shouldThrowException() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> TdumtPatchConverter.jsonToPch(null));
    }

    @Test
    void jsonToPch_whenNoChange_shouldReturnDefaultPatch() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        DbPatchDto patchObject = DbPatchDto.builder().build();


        // WHEN
        Document actualDocument = TdumtPatchConverter.jsonToPch(patchObject);


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
    void jsonToPch_whenRealPatchObject_forContents_shouldReturnPatch() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        DbPatchDto patchObject = FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-ref.mini.json");


        // WHEN
        Document actualDocument = TdumtPatchConverter.jsonToPch(patchObject);


        // THEN
        assertThat(actualDocument).isNotNull();

        NodeList instructions = assertStructureAndReturnInstructions(actualDocument, 1);

        Element instruction = (Element) instructions.item(0);
        assertUpdateDatabaseInstruction(instruction, "CarPhysicsData", "1221657049|1221657049\t864426\t56338407\t");
    }

    @Test
    void jsonToPch_whenRealPatchObject_forContents_andNoEntryRef_shouldReturnPatch() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        DbPatchDto patchObject = FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContents-addAll-noRef.mini.json");


        // WHEN
        Document actualDocument = TdumtPatchConverter.jsonToPch(patchObject);


        // THEN
        assertThat(actualDocument).isNotNull();

        NodeList instructions = assertStructureAndReturnInstructions(actualDocument, 1);

        Element instruction = (Element) instructions.item(0);
        assertUpdateDatabaseInstruction(instruction, "Bots", "57167257=56373256|57167257\t56373256\t600091920\t1\t1\t551683160\t0\t0.5");
    }

    @Test
    void jsonToPch_whenRealPatchObject_forResources_shouldReturnPatch() throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        // GIVEN
        DbPatchDto patchObject = FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateResources.mini.json");


        // WHEN
        Document actualDocument = TdumtPatchConverter.jsonToPch(patchObject);


        // THEN
        assertThat(actualDocument).isNotNull();

        NodeList instructions = assertStructureAndReturnInstructions(actualDocument, 1);

        Element instruction = (Element) instructions.item(0);
        assertUpdateResourceInstruction(instruction, "Bots", "54367256|Brian Molko||33333333|Cindy");
    }

    @Test
    void jsonToPch_whenRealPatchObject_forContentsAndResources_shouldReturnPatch() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        DbPatchDto patchObject = FilesHelper.readObjectFromJsonResourceFile(DbPatchDto.class, "/db/patch/updateContentsAndResources-all.mini.json");


        // WHEN
        Document actualDocument = TdumtPatchConverter.jsonToPch(patchObject);


        // THEN
        assertThat(actualDocument).isNotNull();

        NodeList instructions = assertStructureAndReturnInstructions(actualDocument, 2);

        Element instruction1 = (Element) instructions.item(0);
        assertUpdateDatabaseInstruction(instruction1, "CarPhysicsData", "1221657049|1221657049\t864426\t56338407\t");

        Element instruction2 = (Element) instructions.item(1);
        assertUpdateResourceInstruction(instruction2, "Bots", "54367256|Brian Molko||33333333|Cindy");
    }

    @Test
    void pchToJson_whenNullObject_shouldThrowException() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> TdumtPatchConverter.pchToJson(null));
    }

    @Test
    void pchToJson_whenNoInstruction_shouldReturnEmptyPatchObject() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        Document patchDocument = TdumtPatchConverter.initXmlDocumentFromResource("/db/patch/tdumt/empty.pch");

        // WHEN
        DbPatchDto actualPatchObject = TdumtPatchConverter.pchToJson(patchDocument);

        // THEN
        assertThat(actualPatchObject).isNotNull();
        assertThat(actualPatchObject.getChanges()).isEmpty();
    }

    @Test
    void pchToJson_whenRealPatchDocument_andDuplicateInstructions_shouldReturnPatchObject_withSingleChange() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        Document patchDocument = TdumtPatchConverter.initXmlDocumentFromResource("/db/patch/tdumt/removeAllLines_duplicateInstruction.pch");

        // WHEN
        DbPatchDto actualPatchObject = TdumtPatchConverter.pchToJson(patchDocument);

        // THEN
        assertThat(actualPatchObject).isNotNull();
        assertThat(actualPatchObject.getChanges()).hasSize(1);
    }

    @Test
    void pchToJson_whenRealPatchDocument_forContentsAndResources_shouldReturnPatchObject() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        Document patchDocument = TdumtPatchConverter.initXmlDocumentFromResource("/db/patch/tdumt/updateContentsAndResources.pch");


        // WHEN
        DbPatchDto actualPatchObject = TdumtPatchConverter.pchToJson(patchDocument);


        // THEN
        assertThat(actualPatchObject).isNotNull();
        assertThat(actualPatchObject.getChanges()).hasSize(12); // (2 contents + 10 resources)

        assertThat(actualPatchObject.getChanges()).extracting("type").containsOnly(UPDATE, UPDATE_RES);
        assertThat(actualPatchObject.getChanges()).extracting("topic").containsOnly(BRANDS, CAR_PHYSICS_DATA);
        assertThat(actualPatchObject.getChanges()).extracting("locale").containsOnly(new Object[]{null});
    }

    @Test
    void pchToJson_whenRealPatchDocument_forContents_andCompositeEntryRef_shouldReturnPatchObject() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        Document patchDocument = TdumtPatchConverter.initXmlDocumentFromResource("/db/patch/tdumt/updateContents_compositeRef.pch");


        // WHEN
        DbPatchDto actualPatchObject = TdumtPatchConverter.pchToJson(patchDocument);


        // THEN
        assertThat(actualPatchObject).isNotNull();
        assertThat(actualPatchObject.getChanges()).hasSize(1);

        DbPatchDto.DbChangeDto actualChangeObject = actualPatchObject.getChanges().get(0);
        assertThat(actualChangeObject.getRef()).isNull();
        assertThat(actualChangeObject.getLocale()).isNull();
        assertThat(actualChangeObject.getTopic()).isEqualTo(BOTS);
        assertThat(actualChangeObject.getType()).isEqualTo(UPDATE);
        assertThat(actualChangeObject.getValue()).isNull();
        assertThat(actualChangeObject.getValues()).containsExactly("57167257", "56373256", "600091920", "1", "1", "551683160", "0", "0.5");
    }

    @Test
    void pchToJson_whenRealPatchDocument_forAllLinesRemoval_shouldReturnPatchObject() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        Document patchDocument = TdumtPatchConverter.initXmlDocumentFromResource("/db/patch/tdumt/removeAllLines.pch");


        // WHEN
        DbPatchDto actualPatchObject = TdumtPatchConverter.pchToJson(patchDocument);


        // THEN
        assertThat(actualPatchObject).isNotNull();
        assertThat(actualPatchObject.getChanges()).hasSize(1);

        DbPatchDto.DbChangeDto actualChangeObject = actualPatchObject.getChanges().get(0);
        assertThat(actualChangeObject.getRef()).isNull();
        assertThat(actualChangeObject.getLocale()).isNull();
        assertThat(actualChangeObject.getTopic()).isEqualTo(CAR_PACKS);
        assertThat(actualChangeObject.getType()).isEqualTo(DELETE);
        assertThat(actualChangeObject.getValue()).isNull();
        assertThat(actualChangeObject.getValues()).isNull();
        assertThat(actualChangeObject.getPartialValues()).isNull();
        assertThat(actualChangeObject.getFilterCompounds()).extracting("rank").containsOnly(1);
        assertThat(actualChangeObject.getFilterCompounds()).extracting("value").containsOnly("916575065");
    }

    @Test
    void pchToJson_whenRealPatchDocument_forSetVehicleOnTwoSpots_shouldReturnTwoChangeObjects() throws ParserConfigurationException, IOException, SAXException, URISyntaxException {
        // GIVEN
        Document patchDocument = TdumtPatchConverter.initXmlDocumentFromResource("/db/patch/tdumt/setVehicleOnSpots.pch");


        // WHEN
        DbPatchDto actualPatchObject = TdumtPatchConverter.pchToJson(patchDocument);


        // THEN
        assertThat(actualPatchObject).isNotNull();
        assertThat(actualPatchObject.getChanges()).hasSize(2);

        assertThat(actualPatchObject.getChanges()).extracting("ref").containsOnly("595033714","595033715");
        assertThat(actualPatchObject.getChanges()).extracting("topic").containsOnly(CAR_SHOPS);
        assertThat(actualPatchObject.getChanges()).extracting("type").containsOnly(UPDATE);

        DbFieldValueDto partialValue1 = DbFieldValueDto.fromCouple(11, "916575065" );
        DbFieldValueDto partialValue2 = DbFieldValueDto.fromCouple(13, "916575065" );
        assertThat(actualPatchObject.getChanges()).extracting("partialValues").containsOnly(singletonList(partialValue1), singletonList(partialValue2));
    }

    @Test
    void getContentsValue_whenNoEntryRef_shouldReturnValuesWithCompositeKey() {
        // GIVEN
        List<String> values = asList("000000", "111111", "222222");

        // WHEN
        String actualValue = TdumtPatchConverter.getContentsValue(empty(), values);

        // THEN
        assertThat(actualValue).isEqualTo("000000=111111|000000\t111111\t222222");
    }

    @Test
    void getContentsValue_whenEntryRef_shouldReturnValuesWithSingleKey() {
        // GIVEN
        List<String> values = asList("000000", "111111", "222222");

        // WHEN
        String actualValue = TdumtPatchConverter.getContentsValue(Optional.of("000000"), values);

        // THEN
        assertThat(actualValue).isEqualTo("000000|000000\t111111\t222222");
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
