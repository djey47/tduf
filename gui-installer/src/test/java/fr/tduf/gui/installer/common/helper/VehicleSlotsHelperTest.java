package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.domain.*;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.*;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.SlotKind.ALL;
import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.VehicleKind.DRIVABLE;
import static fr.tduf.gui.installer.domain.Resource.from;
import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VehicleSlotsHelperTest {
    private static final String SLOTREF = "REF";
    private static final String SLOTREF_TDUCP = "300000000";
    private static final String SLOTREF_UNDRIVABLE = "00000000";
    private static final String BRANDREF = "81940960";
    private static final String BRAND_ID_REF = "0000";
    private static final String BRAND_ID = "FORZA";
    private static final String BRAND_NAME_REF = "3333";
    private static final String BRAND_NAME = "FERRARI";

    @Mock
    private BulkDatabaseMiner bulkDatabaseMinerMock;

    @Mock
    private BrandHelper brandHelperMock;

    @InjectMocks
    private VehicleSlotsHelper vehicleSlotsHelper;

    @Before
    public void setUp() {
        mockBrandHelper();
        mockMinerForBrands();
    }

    @Test
    public void classInitializer_shouldPopulateDataFromProps() {
        // GIVEN-WHEN-THEN
        assertThat(VehicleSlotsHelper.getTducpUnlockedSlotRefs()).hasSize(27);

        assertThat(VehicleSlotsHelper.getTducpBikeSlotPattern().matcher("300000000").matches()).isFalse();
        assertThat(VehicleSlotsHelper.getTducpBikeSlotPattern().matcher("400000000").matches()).isTrue();
        assertThat(VehicleSlotsHelper.getTducpCarSlotPattern().matcher("300000000").matches()).isTrue();
        assertThat(VehicleSlotsHelper.getTducpCarSlotPattern().matcher("400000000").matches()).isFalse();
    }

    @Test
    public void getVehicleSlotFromReference_whenSlotNotAvailable_shouldReturnEmpty() {
        // GIVEN
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(SLOTREF, CAR_PHYSICS_DATA)).thenReturn(empty());

        // WHEN
        final Optional<VehicleSlot> actualSlot = VehicleSlotsHelper
                .load(bulkDatabaseMinerMock)
                .getVehicleSlotFromReference(SLOTREF);

        // THEN
        assertThat(actualSlot).isEmpty();
    }

    @Test
    public void getVehicleSlotFromReference() {
        // GIVEN
        String rimSlotRef1 = "RIMREF1";
        String rimSlotRef2 = "RIMREF2";
        String directoryRef = "0000";
        String directory = "DIR";
        String fileNameRef = "1111";
        String fileName = "FILE";
        String realNameRef = "4444";
        String realName = "REALNAME";
        String modelNameRef = "5555";
        String modelName = "MODELNAME";
        String versionNameRef = "6666";
        String versionName = "VERSIONNAME";
        String frontRimFileNameRef1 = "1111-1";
        String frontRimFileName1 = "FILE_F1";
        String rearRimFileNameRef1 = "2222-1";
        String rearRimFileName1 = "FILE_R1";
        String frontRimFileNameRef2 = "1111-2";
        String frontRimFileName2 = "FILE_F2";
        String rearRimFileNameRef2 = "2222-2";
        String rearRimFileName2 = "FILE_R2";
        String interiorRef = "7777";
        String colorNameRef = "8888";
        String colorName = "Azzuro";
        int idCar = 222;
        int idCam = 200;
        float secuOne = 100;
        int secuTwo = 101;
        ContentEntryDto carRimsEntry1 = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF).build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(rimSlotRef1).build())
                .build();
        ContentEntryDto carRimsEntry2 = ContentEntryDto.builder()
                .forId(1)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF).build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(rimSlotRef2).build())
                .build();
        ContentEntryDto physicsEntry = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF).build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(BRANDREF).build())
                .addItem(ContentItemDto.builder().ofFieldRank(9).withRawValue(fileNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(10).withRawValue(rimSlotRef1).build())
                .addItem(ContentItemDto.builder().ofFieldRank(12).withRawValue(realNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(13).withRawValue(modelNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(14).withRawValue(versionNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(98).withRawValue(Integer.toString(idCam)).build())
                .addItem(ContentItemDto.builder().ofFieldRank(100).withRawValue(Float.toString(secuOne)).build())
                .addItem(ContentItemDto.builder().ofFieldRank(101).withRawValue(Integer.toString(secuTwo)).build())
                .addItem(ContentItemDto.builder().ofFieldRank(102).withRawValue(Integer.toString(idCar)).build())
                .build();
        ContentEntryDto rimsEntry1 = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(rimSlotRef1).build())
                .addItem(ContentItemDto.builder().ofFieldRank(13).withRawValue(directoryRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(14).withRawValue(frontRimFileNameRef1).build())
                .addItem(ContentItemDto.builder().ofFieldRank(15).withRawValue(rearRimFileNameRef1).build())
                .build();
        ContentEntryDto rimsEntry2 = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(rimSlotRef2).build())
                .addItem(ContentItemDto.builder().ofFieldRank(13).withRawValue(directoryRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(14).withRawValue(frontRimFileNameRef2).build())
                .addItem(ContentItemDto.builder().ofFieldRank(15).withRawValue(rearRimFileNameRef2).build())
                .build();
        ContentEntryDto carColorsEntry = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF).build())
                .addItem(ContentItemDto.builder().ofFieldRank(3).withRawValue(colorNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(8).withRawValue(interiorRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(9).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(10).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(11).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(12).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(13).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(14).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(15).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(16).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(17).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(18).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(19).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(20).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(21).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(22).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .build();
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(SLOTREF, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, SLOTREF), CAR_RIMS)).thenReturn(
                asList(
                    carRimsEntry1,
                    carRimsEntry2
                ).stream(),
                asList(
                    carRimsEntry1,
                    carRimsEntry2
                ).stream());
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef1, RIMS)).thenReturn(of(rimsEntry1));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef2, RIMS)).thenReturn(of(rimsEntry2));
        when(bulkDatabaseMinerMock.getContentEntryStreamMatchingSimpleCondition(any(DbFieldValueDto.class), eq(CAR_COLORS))).thenReturn(Stream.of(carColorsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(colorNameRef, CAR_COLORS, UNITED_STATES)).thenReturn(of(colorName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(fileNameRef, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(fileName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(realNameRef, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(realName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(modelNameRef, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(modelName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(versionNameRef, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(versionName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(directoryRef, RIMS, UNITED_STATES)).thenReturn(of(directory));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(frontRimFileNameRef1, RIMS, UNITED_STATES)).thenReturn(of(frontRimFileName1));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(rearRimFileNameRef1, RIMS, UNITED_STATES)).thenReturn(of(rearRimFileName1));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(frontRimFileNameRef2, RIMS, UNITED_STATES)).thenReturn(of(frontRimFileName2));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(rearRimFileNameRef2, RIMS, UNITED_STATES)).thenReturn(of(rearRimFileName2));


        // WHEN
        final Optional<VehicleSlot> actualSlot = vehicleSlotsHelper.getVehicleSlotFromReference(SLOTREF);


        // THEN
        assertThat(actualSlot).isPresent();

        VehicleSlot vehicleSlot = actualSlot.get();
        assertThat(vehicleSlot.getRef()).isEqualTo(SLOTREF);
        assertThat(vehicleSlot.getCarIdentifier()).isEqualTo(idCar);
        assertThat(vehicleSlot.getFileName()).isEqualTo(from(fileNameRef, fileName));
        assertThat(vehicleSlot.getBrand().getDisplayedName()).isEqualTo(from(BRAND_NAME_REF, BRAND_NAME));
        assertThat(vehicleSlot.getRealName()).isEqualTo(from(realNameRef, realName));
        assertThat(vehicleSlot.getModelName()).isEqualTo(from(modelNameRef, modelName));
        assertThat(vehicleSlot.getVersionName()).isEqualTo(from(versionNameRef, versionName));
        assertThat(vehicleSlot.getCameraIdentifier()).isEqualTo(idCam);
        assertThat(vehicleSlot.getSecurityOptions()).isEqualTo(SecurityOptions.fromValues(secuOne, secuTwo));

        RimSlot actualDefaultRims = vehicleSlot.getDefaultRims().get();
        assertThat(actualDefaultRims.getRef()).isEqualTo(rimSlotRef1);
        assertThat(actualDefaultRims.getParentDirectoryName()).isEqualTo(from(directoryRef, directory));
        assertThat(actualDefaultRims.getFrontRimInfo().getFileName()).isEqualTo(from(frontRimFileNameRef1, frontRimFileName1));
        assertThat(actualDefaultRims.getRearRimInfo().getFileName()).isEqualTo(from(rearRimFileNameRef1, rearRimFileName1));

        assertThat(vehicleSlot.getAllRimOptionsSorted())
                .hasSize(2)
                .extracting("ref").containsExactly(rimSlotRef1, rimSlotRef2);
        assertThat(vehicleSlot.getAllRimCandidatesSorted()).isEmpty();

        assertThat(vehicleSlot.getPaintJobs()).extracting("rank").containsExactly(1);
        assertThat(vehicleSlot.getPaintJobs()).extracting("name").containsExactly(from(colorNameRef, colorName));
        assertThat(vehicleSlot.getPaintJobs()).extracting("interiorPatternRefs").containsExactly(singletonList(interiorRef));

        Brand expectedBrand = createBrandInformation();
        assertThat(vehicleSlot.getBrand()).isEqualTo(expectedBrand);
    }

    @Test
    public void getVehicleSlotFromReference_whenNewTDUCPSlot_shouldLoadRimCandidates() {
        // GIVEN
        String rimSlotRef1 = "000030001";
        String rimSlotRef2 = "000030002";
        String rimSlotRef3 = "000030003";
        String rimSlotRef4 = "000030004";
        String rimSlotRef5 = "000030005";
        String rimSlotRef6 = "000030006";
        String rimSlotRef7 = "000030007";
        String rimSlotRef8 = "000030008";
        String rimSlotRef9 = "000030009";
        String directoryRef = "0000";
        String directory = "DIR";
        String fileNameRef = "1111";
        String fileName = "FILE";
        String realNameRef = "4444";
        String realName = "REALNAME";
        String modelNameRef = "5555";
        String modelName = "MODELNAME";
        String versionNameRef = "6666";
        String versionName = "VERSIONNAME";
        String frontRimFileNameRef1 = "1111-1";
        String frontRimFileName1 = "FILE_F1";
        String rearRimFileNameRef1 = "2222-1";
        String rearRimFileName1 = "FILE_R1";
        String frontRimFileNameRef2 = "1111-2";
        String frontRimFileName2 = "FILE_F2";
        String rearRimFileNameRef2 = "2222-2";
        String rearRimFileName2 = "FILE_R2";
        String interiorRef = "7777";
        String colorNameRef = "8888";
        String colorName = "Azzuro";
        int idCar = 222;
        int idCam = 200;
        float secuOne = 100;
        int secuTwo = 101;
        ContentEntryDto carRimsEntry1 = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF_TDUCP).build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(rimSlotRef1).build())
                .build();
        ContentEntryDto physicsEntry = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF_TDUCP).build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(BRANDREF).build())
                .addItem(ContentItemDto.builder().ofFieldRank(9).withRawValue(fileNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(10).withRawValue(rimSlotRef1).build())
                .addItem(ContentItemDto.builder().ofFieldRank(12).withRawValue(realNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(13).withRawValue(modelNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(14).withRawValue(versionNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(98).withRawValue(Integer.toString(idCam)).build())
                .addItem(ContentItemDto.builder().ofFieldRank(100).withRawValue(Float.toString(secuOne)).build())
                .addItem(ContentItemDto.builder().ofFieldRank(101).withRawValue(Integer.toString(secuTwo)).build())
                .addItem(ContentItemDto.builder().ofFieldRank(102).withRawValue(Integer.toString(idCar)).build())
                .build();
        ContentEntryDto rimsEntry1 = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(rimSlotRef1).build())
                .addItem(ContentItemDto.builder().ofFieldRank(13).withRawValue(directoryRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(14).withRawValue(frontRimFileNameRef1).build())
                .addItem(ContentItemDto.builder().ofFieldRank(15).withRawValue(rearRimFileNameRef1).build())
                .build();
        ContentEntryDto rimsEntry2 = ContentEntryDto.builder()
                .forId(1)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(rimSlotRef2).build())
                .addItem(ContentItemDto.builder().ofFieldRank(13).withRawValue(directoryRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(14).withRawValue(frontRimFileNameRef2).build())
                .addItem(ContentItemDto.builder().ofFieldRank(15).withRawValue(rearRimFileNameRef2).build())
                .build();
        ContentEntryDto carColorsEntry = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF_TDUCP).build())
                .addItem(ContentItemDto.builder().ofFieldRank(3).withRawValue(colorNameRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(8).withRawValue(interiorRef).build())
                .addItem(ContentItemDto.builder().ofFieldRank(9).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(10).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(11).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(12).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(13).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(14).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(15).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(16).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(17).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(18).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(19).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(20).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(21).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(ContentItemDto.builder().ofFieldRank(22).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .build();
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(SLOTREF_TDUCP, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, SLOTREF_TDUCP), CAR_RIMS)).thenReturn(
                singletonList(carRimsEntry1).stream(),
                singletonList(carRimsEntry1).stream());
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef1, RIMS)).thenReturn(of(rimsEntry1));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef2, RIMS)).thenReturn(of(rimsEntry2));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef3, RIMS)).thenReturn(empty());
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef4, RIMS)).thenReturn(empty());
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef5, RIMS)).thenReturn(empty());
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef6, RIMS)).thenReturn(empty());
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef7, RIMS)).thenReturn(empty());
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef8, RIMS)).thenReturn(empty());
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef9, RIMS)).thenReturn(empty());
        when(bulkDatabaseMinerMock.getContentEntryStreamMatchingSimpleCondition(any(DbFieldValueDto.class), eq(CAR_COLORS))).thenReturn(Stream.of(carColorsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(colorNameRef, CAR_COLORS, UNITED_STATES)).thenReturn(of(colorName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(fileNameRef, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(fileName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(realNameRef, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(realName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(modelNameRef, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(modelName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(versionNameRef, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(versionName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(directoryRef, RIMS, UNITED_STATES)).thenReturn(of(directory));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(frontRimFileNameRef1, RIMS, UNITED_STATES)).thenReturn(of(frontRimFileName1));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(rearRimFileNameRef1, RIMS, UNITED_STATES)).thenReturn(of(rearRimFileName1));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(frontRimFileNameRef2, RIMS, UNITED_STATES)).thenReturn(of(frontRimFileName2));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(rearRimFileNameRef2, RIMS, UNITED_STATES)).thenReturn(of(rearRimFileName2));


        // WHEN
        final Optional<VehicleSlot> actualSlot = vehicleSlotsHelper.getVehicleSlotFromReference(SLOTREF_TDUCP);


        // THEN
        assertThat(actualSlot).isPresent();

        VehicleSlot vehicleSlot = actualSlot.get();
        assertThat(vehicleSlot.getAllRimOptionsSorted())
                .hasSize(1)
                .extracting("ref").containsExactly(rimSlotRef1);
        assertThat(vehicleSlot.getAllRimCandidatesSorted())
                .hasSize(2)
                .extracting("ref").containsExactly(rimSlotRef1, rimSlotRef2);
    }

    @Test
    public void getVehicleName_whenRealNameAvailable() throws Exception {
        // GIVEN
        String realName = "realName";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(SLOTREF)
                .withRealName(from("", realName))
                .withModelName(from(DatabaseConstants.RESOURCE_REF_UNKNOWN_VEHICLE_NAME, DatabaseConstants.RESOURCE_VALUE_NONE))
                .withVersionName(from(DatabaseConstants.RESOURCE_REF_UNKNOWN_VEHICLE_NAME, DatabaseConstants.RESOURCE_VALUE_NONE))
                .build();

        // WHEN
        final String actualName = VehicleSlotsHelper.getVehicleName(vehicleSlot);

        // THEN
        assertThat(actualName).isEqualTo(realName);
    }

    @Test
    public void getVehicleName_whenRealNameUnavailable() throws Exception {
        // GIVEN
        String modelName = "360";
        String versionName = "Challenge Stradale";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(SLOTREF)
                .withBrand(createBrandInformation())
                .withModelName(from("", modelName))
                .withVersionName(from("", versionName))
                .build();

        // WHEN
        final String actualName = VehicleSlotsHelper.getVehicleName(vehicleSlot);

        // THEN
        assertThat(actualName).isEqualTo("FERRARI 360 Challenge Stradale");
    }

    @Test
    public void getVehicleSlots_whenNoDrivableVehicle_shouldReturnEmptyList() {
        // GIVEN
        ContentItemDto refItem = ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF_UNDRIVABLE).build();
        ContentItemDto groupItem = ContentItemDto.builder().ofFieldRank(5).withRawValue("92900264").build();
        ContentEntryDto undrivableEntry = ContentEntryDto.builder().addItem(refItem, groupItem).build();
        DbDataDto dataObject = DbDataDto.builder().addEntry(undrivableEntry).build();
        DbDto topicObject = DbDto.builder().withData(dataObject).build();

        when(bulkDatabaseMinerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(of(topicObject));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(SLOTREF_UNDRIVABLE, CAR_PHYSICS_DATA)).thenReturn(of(undrivableEntry));


        // WHEN
        final List<VehicleSlot> actualSlots = vehicleSlotsHelper.getVehicleSlots(ALL, DRIVABLE);


        // THEN
        assertThat(actualSlots).isEmpty();
    }

    @Test
    public void getVehicleSlots_when1DrivableVehicle_shouldReturnIt() {
        // GIVEN
        ContentItemDto refItem1 = ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF_UNDRIVABLE).build();
        ContentItemDto brandItem1 = ContentItemDto.builder().ofFieldRank(2).withRawValue(BRANDREF).build();
        ContentItemDto groupItem1 = ContentItemDto.builder().ofFieldRank(5).withRawValue("92900264").build();
        ContentItemDto refItem2 = ContentItemDto.builder().ofFieldRank(1).withRawValue(SLOTREF).build();
        ContentItemDto brandItem2 = ContentItemDto.builder().ofFieldRank(2).withRawValue(BRANDREF).build();
        ContentItemDto groupItem2 = ContentItemDto.builder().ofFieldRank(5).withRawValue("77800264").build();
        ContentEntryDto undrivableEntry = ContentEntryDto.builder().forId(0).addItem(refItem1, brandItem1, groupItem1).build();
        ContentEntryDto drivableEntry = ContentEntryDto.builder().forId(1).addItem(refItem2, brandItem2, groupItem2).build();
        DbDataDto dataObject = DbDataDto.builder().addEntry(undrivableEntry, drivableEntry).build();
        DbDto topicObject = DbDto.builder().withData(dataObject).build();

        when(bulkDatabaseMinerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(of(topicObject));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(SLOTREF_UNDRIVABLE, CAR_PHYSICS_DATA)).thenReturn(of(undrivableEntry));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(SLOTREF, CAR_PHYSICS_DATA)).thenReturn(of(drivableEntry));
        when(bulkDatabaseMinerMock.getContentEntryStreamMatchingSimpleCondition(any(DbFieldValueDto.class), any(DbDto.Topic.class))).thenReturn(Stream.empty(), Stream.empty());


        // WHEN
        final List<VehicleSlot> actualSlots = vehicleSlotsHelper.getVehicleSlots(ALL, DRIVABLE);


        // THEN
        assertThat(actualSlots).hasSize(1);
        assertThat(actualSlots.get(0).getRef()).isEqualTo(SLOTREF);
    }

    @Test
    public void getBankFileName_forExteriorModel() {
        // GIVEN
        String resourceValue = "RX8";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(SLOTREF)
                .withFileName(from("", resourceValue)).build();

        // WHEN
        String actualBankFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, EXTERIOR_MODEL, true);

        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8.bnk");
    }

    @Test
    public void getBankFileName_forAudio() {
        // GIVEN
        String resourceValue = "RX8";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(SLOTREF)
                .withFileName(from("", resourceValue)).build();

        // WHEN
        String actualBankFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, SOUND, true);

        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_audio.bnk");
    }

    @Test
    public void getBankFileName_forInteriorModel() {
        // GIVEN
        String resourceValue = "RX8";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(SLOTREF)
                .withFileName(from("", resourceValue)).build();

        // WHEN
        String actualBankFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, INTERIOR_MODEL, true);

        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_I.bnk");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBankFileName_forFrontRimsModel_shouldReturnDefaultFrontRimFileName() {
        // GIVEN
        RimSlot rims = RimSlot.builder()
                .atRank(0)
                .withRef("22222222")
                .setDefaultRims(true)
                .build();
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef("11111111")
                .addRim(rims)
                .build();

        // WHEN
        VehicleSlotsHelper.getBankFileName(vehicleSlot, FRONT_RIM, true);

        // THEN: IAE
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBankFileName_forRearRimsModel_shouldThrowException() {
        // GIVEN
        RimSlot rims = RimSlot.builder()
                .withRef("22222222")
                .atRank(0)
                .setDefaultRims(true)
                .build();
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef("11111111")
                .addRim(rims)
                .build();

        // WHEN
        VehicleSlotsHelper.getBankFileName(vehicleSlot, REAR_RIM, true);

        // THEN: IAE
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRimBankFileName_whenNoRimBankType_shouldThrowException() {
        // GIVEN-WHEN
        VehicleSlotsHelper.getRimBankFileName(null, EXTERIOR_MODEL, 0, false);

        // THEN:IAE
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRimBankFileName_whenNotEnoughRims_shouldThrowException() {
        // GIVEN
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef("11111111")
                .build();

        // WHEN
        VehicleSlotsHelper.getRimBankFileName(vehicleSlot, FRONT_RIM, 1, false);

        // THEN:IAE
    }

    @Test
    public void getRimBankFileName_forFrontRims() {
        // GIVEN
        String rimsResourceValue1 = "RX8_F_01";
        String rimsResourceValue2 = "RX8_F_02";

        RimSlot.RimInfo frontRimInfo = RimSlot.RimInfo.builder()
                .withFileName(from("33333333", rimsResourceValue1))
                .build();
        RimSlot.RimInfo frontRimInfo2 = RimSlot.RimInfo.builder()
                .withFileName(from("33333333-2", rimsResourceValue2))
                .build();
        RimSlot rims1 = RimSlot.builder()
                .atRank(1)
                .withRef("22222222")
                .withRimsInformation(frontRimInfo, null)
                .setDefaultRims(true)
                .build();
        RimSlot rims2 = RimSlot.builder()
                .atRank(2)
                .withRef("22222222-2")
                .withRimsInformation(frontRimInfo2, null)
                .build();
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef("11111111")
                .addRim(rims1)
                .addRim(rims2)
                .build();

        // WHEN
        final String actualFileName = VehicleSlotsHelper.getRimBankFileName(vehicleSlot, FRONT_RIM, 2, true);

        // THEN
        assertThat(actualFileName).isEqualTo("RX8_F_02.bnk");
    }

    private void mockMinerForBrands() {
        ContentEntryDto brandsEntry = ContentEntryDto.builder()
                .forId(0)
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(BRANDREF).build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(BRAND_ID_REF).build())
                .addItem(ContentItemDto.builder().ofFieldRank(3).withRawValue(BRAND_NAME_REF).build())
                .build();
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(BRANDREF, BRANDS)).thenReturn(of(brandsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(0, 3, BRANDS, UNITED_STATES)).thenReturn(of(BRAND_NAME));
    }

    private void mockBrandHelper() {
        vehicleSlotsHelper.setBrandHelper(brandHelperMock);

        Brand brand = createBrandInformation();
        when(brandHelperMock.getBrandFromReference(BRANDREF)).thenReturn(of(brand));
    }

    private static Brand createBrandInformation() {
        return Brand.builder()
                .withReference(BRANDREF)
                .withIdentifier(from(BRAND_ID_REF, BRAND_ID))
                .withDisplayedName(from(BRAND_NAME_REF, BRAND_NAME))
                .build();
    }
}
