package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.gui.installer.domain.RimSlot;
import fr.tduf.gui.installer.domain.VehicleSlot;
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

import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class VehicleSlotsHelperTest {

    @Mock
    private BulkDatabaseMiner bulkDatabaseMinerMock;

    @InjectMocks
    private VehicleSlotsHelper vehicleSlotsHelper;

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
        String rimSlotRef = "RIMREF";
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
        String frontRimFileNameRef = "1111";
        String frontRimFileName = "FILE_F";
        String rearRimFileNameRef = "2222";
        String rearRimFileName = "FILE_R";
        int idCar = 222;
        DbDataDto.Entry brandsEntry = DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(brandSlotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(3).withRawValue(brandNameRef).build())
                .build();
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(slotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(2).withRawValue(brandSlotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(9).withRawValue(fileNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(10).withRawValue(rimSlotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(12).withRawValue(realNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(13).withRawValue(modelNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(14).withRawValue(versionNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(102).withRawValue(Integer.valueOf(idCar).toString()).build())
                .build();
        DbDataDto.Entry rimsEntry = DbDataDto.Entry.builder()
                .forId(0)
                .addItem(DbDataDto.Item.builder().ofFieldRank(1).withRawValue(rimSlotRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(13).withRawValue(directoryRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(14).withRawValue(frontRimFileNameRef).build())
                .addItem(DbDataDto.Item.builder().ofFieldRank(15).withRawValue(rearRimFileNameRef).build())
                .build();
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(brandSlotRef, BRANDS)).thenReturn(of(brandsEntry));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotRef, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotRef, RIMS)).thenReturn(of(rimsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(0, 3, BRANDS, UNITED_STATES)).thenReturn(of(brandName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(0, 9, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(fileName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(0, 12, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(realName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(0, 13, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(modelName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(0, 14, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(versionName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(directoryRef, RIMS, UNITED_STATES)).thenReturn(of(directory));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(frontRimFileNameRef, RIMS, UNITED_STATES)).thenReturn(of(frontRimFileName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(rearRimFileNameRef, RIMS, UNITED_STATES)).thenReturn(of(rearRimFileName));

        // WHEN
        final Optional<VehicleSlot> actualSlot = VehicleSlotsHelper
                .load(bulkDatabaseMinerMock)
                .getVehicleSlotFromReference(slotRef);

        // THEN
        assertThat(actualSlot).isPresent();
        assertThat(actualSlot.get().getRef()).isEqualTo(slotRef);
        assertThat(actualSlot.get().getCarIdentifier()).isEqualTo(idCar);
        assertThat(actualSlot.get().getFileName()).isEqualTo(Resource.from(fileNameRef, fileName));
        assertThat(actualSlot.get().getBrandName()).isEqualTo(Resource.from("", brandName));
        assertThat(actualSlot.get().getRealName()).isEqualTo(Resource.from(realNameRef, realName));
        assertThat(actualSlot.get().getModelName()).isEqualTo(Resource.from(modelNameRef, modelName));
        assertThat(actualSlot.get().getVersionName()).isEqualTo(Resource.from(versionNameRef, versionName));
        assertThat(actualSlot.get().getDefaultRims().getRef()).isEqualTo(rimSlotRef);
        assertThat(actualSlot.get().getDefaultRims().getParentDirectoryName()).isEqualTo(Resource.from(directoryRef, directory));
        assertThat(actualSlot.get().getDefaultRims().getFrontRimInfo().getFileName()).isEqualTo(Resource.from(frontRimFileNameRef, frontRimFileName));
        assertThat(actualSlot.get().getDefaultRims().getRearRimInfo().getFileName()).isEqualTo(Resource.from(rearRimFileNameRef, rearRimFileName));
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
    public void getVehicleName_whenBrandNameUnavailable() throws Exception {
        // GIVEN
        String slotReference = "REF";
        String modelName = "Brera";
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(slotReference)
                .withModelName(Resource.from("", modelName))
                .withVersionName(Resource.from(DatabaseConstants.RESOURCE_REF_UNKNOWN_VEHICLE_NAME, DatabaseConstants.RESOURCE_VALUE_NONE))
                .build();

        // WHEN
        final String actualName = VehicleSlotsHelper.getVehicleName(vehicleSlot);

        // THEN
        assertThat(actualName).isEqualTo("Brera");
    }

   @Test
    public void getDrivableVehicleSlotEntries_when1DrivableVehicle_shouldReturnIt() {
        // GIVEN
        String undrivableRef = "00000000";
        String drivableRef = "11111111";
        DbDataDto.Item blankItem = DbDataDto.Item.builder().ofFieldRank(2).build();
        DbDataDto.Item refItem1 = DbDataDto.Item.builder().ofFieldRank(1).withRawValue(undrivableRef).build();
        DbDataDto.Item groupItem1 = DbDataDto.Item.builder().ofFieldRank(5).withRawValue("92900264").build();
        DbDataDto.Item refItem2 = DbDataDto.Item.builder().ofFieldRank(1).withRawValue(drivableRef).build();
        DbDataDto.Item groupItem2 = DbDataDto.Item.builder().ofFieldRank(5).withRawValue("77800264").build();
        DbDataDto.Entry undrivableEntry = DbDataDto.Entry.builder().addItem(refItem1, blankItem, blankItem, blankItem, groupItem1).build();
        DbDataDto.Entry drivableEntry = DbDataDto.Entry.builder().addItem(refItem2, blankItem, blankItem, blankItem, groupItem2).build();
        DbDataDto dataObject = DbDataDto.builder().addEntry(undrivableEntry, drivableEntry).build();
        DbDto topicObject = DbDto.builder().withData(dataObject).build();

        when(bulkDatabaseMinerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(of(topicObject));


        // WHEN
        final List<DbDataDto.Entry> actualEntries = vehicleSlotsHelper.getDrivableVehicleSlotEntries();


        // THEN
        assertThat(actualEntries).hasSize(1);
        assertThat(actualEntries.get(0).getItemAtRank(1).get().getRawValue()).isEqualTo(drivableRef);
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

    @Test
    public void getBankFileName_forFrontRimsModel() {
        // GIVEN
        String slotReference = "11111111";
        String rimSlotReference = "22222222";
        String resourceRef = "33333333";
        String rimsResourceValue = "RX8_F_01";
        RimSlot.RimInfo frontRimInfo = RimSlot.RimInfo.builder()
                .withFileName(Resource.from(resourceRef, rimsResourceValue))
                .build();
        RimSlot rims = RimSlot.builder()
                .withRef(rimSlotReference)
                .withRimsInformation(frontRimInfo, null)
                .build();
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(slotReference)
                .withDefaultRims(rims)
                .build();

        // WHEN
        String actualBankFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, FRONT_RIM, true);

        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_F_01.bnk");
    }

    @Test
    public void getBankFileName_forRearRimsModel() {
        // GIVEN
        String slotReference = "11111111";
        String rimSlotReference = "22222222";
        String resourceRef = "33333333";
        String rimsResourceValue = "RX8_R_01";
        RimSlot.RimInfo rearRimInfo = RimSlot.RimInfo.builder()
                .withFileName(Resource.from(resourceRef, rimsResourceValue))
                .build();
        RimSlot rims = RimSlot.builder()
                .withRef(rimSlotReference)
                .withRimsInformation(null, rearRimInfo)
                .build();
        VehicleSlot vehicleSlot = VehicleSlot.builder()
                .withRef(slotReference)
                .withDefaultRims(rims)
                .build();

        // WHEN
        String actualBankFileName = VehicleSlotsHelper.getBankFileName(vehicleSlot, REAR_RIM, true);

        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_R_01.bnk");
    }
}
