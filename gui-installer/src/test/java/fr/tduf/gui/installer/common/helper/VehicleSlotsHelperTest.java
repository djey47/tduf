package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.gui.installer.domain.RimSlot;
import fr.tduf.gui.installer.domain.SecurityOptions;
import fr.tduf.gui.installer.domain.VehicleSlot;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
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
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class VehicleSlotsHelperTest {

    @Mock
    private BulkDatabaseMiner bulkDatabaseMinerMock;

    @InjectMocks
    private VehicleSlotsHelper vehicleSlotsHelper;

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
        String slotReference = "REF";
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(empty());

        // WHEN
        final Optional<VehicleSlot> actualSlot = VehicleSlotsHelper
                .load(bulkDatabaseMinerMock)
                .getVehicleSlotFromReference(slotReference);

        // THEN
        assertThat(actualSlot).isEmpty();
    }

    @Test
    public void getVehicleSlotFromReference() {
        // GIVEN
        String slotRef = "REF";
        String rimSlotRef1 = "RIMREF1";
        String rimSlotRef2 = "RIMREF2";
        String brandSlotRef = "BRANDREF";
        String directoryRef = "0000";
        String directory = "DIR";
        String fileNameRef = "1111";
        String fileName = "FILE";
        String brandNameRef = "3333";
        String brandName = "BRAND";
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
        DbDataDto.Entry brandsEntry = DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(brandSlotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(3).withRawValue(brandNameRef).build())
                .build();
        DbDataDto.Entry carRimsEntry1 = DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(slotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(2).withRawValue(rimSlotRef1).build())
                .build();
        DbDataDto.Entry carRimsEntry2 = DbDataDto.Entry.builder()
                .forId(1)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(slotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(2).withRawValue(rimSlotRef2).build())
                .build();
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(slotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(2).withRawValue(brandSlotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(9).withRawValue(fileNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(10).withRawValue(rimSlotRef1).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(12).withRawValue(realNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(13).withRawValue(modelNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(14).withRawValue(versionNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(98).withRawValue(Integer.toString(idCam)).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(100).withRawValue(Float.toString(secuOne)).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(101).withRawValue(Integer.toString(secuTwo)).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(102).withRawValue(Integer.toString(idCar)).build())
                .build();
        DbDataDto.Entry rimsEntry1 = DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(rimSlotRef1).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(13).withRawValue(directoryRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(14).withRawValue(frontRimFileNameRef1).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(15).withRawValue(rearRimFileNameRef1).build())
                .build();
        DbDataDto.Entry rimsEntry2 = DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(rimSlotRef2).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(13).withRawValue(directoryRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(14).withRawValue(frontRimFileNameRef2).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(15).withRawValue(rearRimFileNameRef2).build())
                .build();
        DbDataDto.Entry carColorsEntry = DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(slotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(3).withRawValue(colorNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(8).withRawValue(interiorRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(9).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(10).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(11).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(12).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(13).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(14).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(15).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(16).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(17).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(18).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(19).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(20).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(21).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(22).withRawValue(DatabaseConstants.REF_NO_INTERIOR).build())
                .build();
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(brandSlotRef, BRANDS)).thenReturn(of(brandsEntry));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotRef, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentEntryStreamMatchingSimpleCondition(DbFieldValueDto.fromCouple(DatabaseConstants.FIELD_RANK_CAR_REF, slotRef), CAR_RIMS)).thenReturn(
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
        when(bulkDatabaseMinerMock.getContentEntriesMatchingCriteria(anyListOf(DbFieldValueDto.class), eq(CAR_COLORS))).thenReturn(singletonList(carColorsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(0, 3, BRANDS, UNITED_STATES)).thenReturn(of(brandName));
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
        final Optional<VehicleSlot> actualSlot = VehicleSlotsHelper
                .load(bulkDatabaseMinerMock)
                .getVehicleSlotFromReference(slotRef);


        // THEN
        assertThat(actualSlot).isPresent();

        VehicleSlot vehicleSlot = actualSlot.get();
        assertThat(vehicleSlot.getRef()).isEqualTo(slotRef);
        assertThat(vehicleSlot.getCarIdentifier()).isEqualTo(idCar);
        assertThat(vehicleSlot.getFileName()).isEqualTo(Resource.from(fileNameRef, fileName));
        assertThat(vehicleSlot.getBrandName()).isEqualTo(Resource.from("", brandName));
        assertThat(vehicleSlot.getRealName()).isEqualTo(Resource.from(realNameRef, realName));
        assertThat(vehicleSlot.getModelName()).isEqualTo(Resource.from(modelNameRef, modelName));
        assertThat(vehicleSlot.getVersionName()).isEqualTo(Resource.from(versionNameRef, versionName));
        assertThat(vehicleSlot.getCameraIdentifier()).isEqualTo(idCam);
        assertThat(vehicleSlot.getSecurityOptions()).isEqualTo(SecurityOptions.fromValues(secuOne, secuTwo));

        RimSlot actualDefaultRims = vehicleSlot.getDefaultRims().get();
        assertThat(actualDefaultRims.getRef()).isEqualTo(rimSlotRef1);
        assertThat(actualDefaultRims.getParentDirectoryName()).isEqualTo(Resource.from(directoryRef, directory));
        assertThat(actualDefaultRims.getFrontRimInfo().getFileName()).isEqualTo(Resource.from(frontRimFileNameRef1, frontRimFileName1));
        assertThat(actualDefaultRims.getRearRimInfo().getFileName()).isEqualTo(Resource.from(rearRimFileNameRef1, rearRimFileName1));

        assertThat(vehicleSlot.getPaintJobs()).extracting("rank").containsExactly(1);
        assertThat(vehicleSlot.getPaintJobs()).extracting("name").containsExactly(Resource.from(colorNameRef, colorName));
        assertThat(vehicleSlot.getPaintJobs()).extracting("interiorPatternRefs").containsExactly(singletonList(interiorRef));
    }

    @Test
    public void getVehicleName_whenRealNameAvailable() throws Exception {
        // GIVEN
        String slotReference = "REF";
        String realName = "realName";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(slotReference)
                .withRealName(Resource.from("", realName))
                .withModelName(Resource.from(DatabaseConstants.RESOURCE_REF_UNKNOWN_VEHICLE_NAME, DatabaseConstants.RESOURCE_VALUE_NONE))
                .withVersionName(Resource.from(DatabaseConstants.RESOURCE_REF_UNKNOWN_VEHICLE_NAME, DatabaseConstants.RESOURCE_VALUE_NONE))
                .build();

        // WHEN
        final String actualName = VehicleSlotsHelper.getVehicleName(vehicleSlot);

        // THEN
        assertThat(actualName).isEqualTo(realName);
    }

    @Test
    public void getVehicleName_whenRealNameUnavailable() throws Exception {
        // GIVEN
        String slotReference = "REF";
        String brandName = "Alfa-Romeo";
        String modelName = "Brera";
        String versionName = "2.0 SkyView";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(slotReference)
                .withBrandName(Resource.from("", brandName))
                .withModelName(Resource.from("", modelName))
                .withVersionName(Resource.from("", versionName))
                .build();

        // WHEN
        final String actualName = VehicleSlotsHelper.getVehicleName(vehicleSlot);

        // THEN
        assertThat(actualName).isEqualTo("Alfa-Romeo Brera 2.0 SkyView");
    }

    @Test
    public void getVehicleSlots_whenNoDrivableVehicle_shouldReturnEmptyList() {
        // GIVEN
        String undrivableRef = "00000000";
        DbDataDto.Item refItem = DbDataDto.Item.builder().ofFieldRank(1).withRawValue(undrivableRef).build();
        DbDataDto.Item groupItem = DbDataDto.Item.builder().ofFieldRank(5).withRawValue("92900264").build();
        DbDataDto.Entry undrivableEntry = DbDataDto.Entry.builder().addItem(refItem, groupItem).build();
        DbDataDto dataObject = DbDataDto.builder().addEntry(undrivableEntry).build();
        DbDto topicObject = DbDto.builder().withData(dataObject).build();

        when(bulkDatabaseMinerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(of(topicObject));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(undrivableRef, CAR_PHYSICS_DATA)).thenReturn(of(undrivableEntry));


        // WHEN
        final List<VehicleSlot> actualSlots = vehicleSlotsHelper.getVehicleSlots(ALL, DRIVABLE);


        // THEN
        assertThat(actualSlots).isEmpty();
    }

    @Test
    public void getVehicleSlots_when1DrivableVehicle_shouldReturnIt() {
        // GIVEN
        String undrivableRef = "00000000";
        String drivableRef = "11111111";
        DbDataDto.Item refItem1 = DbDataDto.Item.builder().ofFieldRank(1).withRawValue(undrivableRef).build();
        DbDataDto.Item groupItem1 = DbDataDto.Item.builder().ofFieldRank(5).withRawValue("92900264").build();
        DbDataDto.Item refItem2 = DbDataDto.Item.builder().ofFieldRank(1).withRawValue(drivableRef).build();
        DbDataDto.Item groupItem2 = DbDataDto.Item.builder().ofFieldRank(5).withRawValue("77800264").build();
        DbDataDto.Entry undrivableEntry = DbDataDto.Entry.builder().addItem(refItem1, groupItem1).build();
        DbDataDto.Entry drivableEntry = DbDataDto.Entry.builder().addItem(refItem2, groupItem2).build();
        DbDataDto dataObject = DbDataDto.builder().addEntry(undrivableEntry, drivableEntry).build();
        DbDto topicObject = DbDto.builder().withData(dataObject).build();

        when(bulkDatabaseMinerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(of(topicObject));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(undrivableRef, CAR_PHYSICS_DATA)).thenReturn(of(undrivableEntry));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(drivableRef, CAR_PHYSICS_DATA)).thenReturn(of(drivableEntry));
        when(bulkDatabaseMinerMock.getContentEntryStreamMatchingSimpleCondition(any(DbFieldValueDto.class), any(DbDto.Topic.class))).thenReturn(Stream.empty(), Stream.empty());


        // WHEN
        final List<VehicleSlot> actualSlots = vehicleSlotsHelper.getVehicleSlots(ALL, DRIVABLE);


        // THEN
        assertThat(actualSlots).hasSize(1);
        assertThat(actualSlots.get(0).getRef()).isEqualTo(drivableRef);
    }

    @Test
    public void getBankFileName_forExteriorModel() {
        // GIVEN
        String slotReference = "11111111";
        String resourceValue = "RX8";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(slotReference)
                .withFileName(Resource.from("", resourceValue)).build();

        // WHEN
        String actualBankFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, EXTERIOR_MODEL, true);

        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8.bnk");
    }

    @Test
    public void getBankFileName_forAudio() {
        // GIVEN
        String slotReference = "11111111";
        String resourceValue = "RX8";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(slotReference)
                .withFileName(Resource.from("", resourceValue)).build();

        // WHEN
        String actualBankFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, SOUND, true);

        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_audio.bnk");
    }

    @Test
    public void getBankFileName_forInteriorModel() {
        // GIVEN
        String slotReference = "11111111";
        String resourceValue = "RX8";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(slotReference)
                .withFileName(Resource.from("", resourceValue)).build();

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
                .withFileName(Resource.from("33333333", rimsResourceValue1))
                .build();
        RimSlot.RimInfo frontRimInfo2 = RimSlot.RimInfo.builder()
                .withFileName(Resource.from("33333333-2", rimsResourceValue2))
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
}
