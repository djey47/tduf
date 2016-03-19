package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.common.DatabaseConstants;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class VehicleSlotsHelperTest {

    @Mock
    private BulkDatabaseMiner bulkDatabaseMinerMock;

    @InjectMocks
    private VehicleSlotsHelper vehicleSlotsHelper;

    @Test
    public void getVehicleName_whenInformationUnavailable() throws Exception {
        // GIVEN
        String slotReference = "REF";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(Optional.empty());


        // WHEN
        final String actualName = vehicleSlotsHelper.getVehicleName(slotReference);


        // THEN
        assertThat(actualName).isEqualTo("N/A");
    }

    @Test
    public void getVehicleName_whenRealNameAvailable() throws Exception {
        // GIVEN
        String slotReference = "REF";
        String realName = "realName";

        DbDataDto.Entry contentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 12, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(realName));


        // WHEN
        final String actualName = vehicleSlotsHelper.getVehicleName(slotReference);


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

        DbDataDto.Entry contentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbDataDto.Entry remoteContentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));
        when(bulkDatabaseMinerMock.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, 2, 1, BRANDS)).thenReturn(of(remoteContentEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 3, BRANDS, UNITED_STATES)).thenReturn(of(brandName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 12, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of("??"));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 13, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(modelName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 14, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(versionName));


        // WHEN
        final String actualName = vehicleSlotsHelper.getVehicleName(slotReference);


        // THEN
        assertThat(actualName).isEqualTo("Alfa-Romeo Brera 2.0 SkyView");
    }

    @Test
    public void getVehicleName_whenBrandNameUnavailable() throws Exception {
        // GIVEN
        String slotReference = "REF";
        String modelName = "Brera";

        DbDataDto.Entry contentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbDataDto.Entry remoteContentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));
        when(bulkDatabaseMinerMock.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, 2, 1, BRANDS)).thenReturn(of(remoteContentEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 3, BRANDS, UNITED_STATES)).thenReturn(empty());
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 12, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of("??"));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 13, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(modelName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 14, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(empty());


        // WHEN
        final String actualName = vehicleSlotsHelper.getVehicleName(slotReference);


        // THEN
        assertThat(actualName).isEqualTo("Brera");
    }

    @Test
    public void getVehicleName_whenVersionNameUnavailable() throws Exception {
        // GIVEN
        String slotReference = "REF";
        String brandName = "Alfa-Romeo";
        String modelName = "Brera";

        DbDataDto.Entry contentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbDataDto.Entry remoteContentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));
        when(bulkDatabaseMinerMock.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, 2, 1, BRANDS)).thenReturn(of(remoteContentEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 3, BRANDS, UNITED_STATES)).thenReturn(of(brandName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 12, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of("??"));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 13, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(modelName));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 14, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of("??"));


        // WHEN
        final String actualName = vehicleSlotsHelper.getVehicleName(slotReference);


        // THEN
        assertThat(actualName).isEqualTo("Alfa-Romeo Brera");
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
    public void getDefaultRimDirectoryForVehicle_whenSlotExists() {
        // GIVEN
        String slotReference = "11111111";
        String rimRef = "22222222";
        long entryId = 1;
        DbDataDto.Item physicsItem = DbDataDto.Item.builder()
                .ofFieldRank(10)
                .withRawValue(rimRef)
                .build();
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(entryId)
                .addItem(physicsItem)
                .build();
        DbDataDto.Entry rimsEntry = DbDataDto.Entry.builder()
                .forId(entryId)
                .build();
        String resourceValue = "Toyota";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimRef, RIMS)).thenReturn(of(rimsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(entryId, 13, RIMS, UNITED_STATES)).thenReturn(of(resourceValue));


        // WHEN
        String actualRimDirectory = vehicleSlotsHelper.getDefaultRimDirectoryForVehicle(slotReference);


        // THEN
        assertThat(actualRimDirectory).isEqualTo("Toyota");
    }

    @Test
    public void getDefaultRimDirectoryForVehicle_whenSlotDoesNotExist_shouldReturnDefault() {
        // GIVEN
        String slotReference = "11111111";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(empty());


        // WHEN
        String actualRimDirectory = vehicleSlotsHelper.getDefaultRimDirectoryForVehicle(slotReference);


        // THEN
        assertThat(actualRimDirectory).isEqualTo(DatabaseConstants.RESOURCE_VALUE_DEFAULT);
    }

    @Test
    public void getBankFileName_whenSlotDoesNotExist_shouldReturnUnavailable() {
        // GIVEN
        String slotReference = "11111111";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(empty());


        // WHEN
        String actualBankFileName = vehicleSlotsHelper.getBankFileName(slotReference, EXTERIOR_MODEL);


        // THEN
        assertThat(actualBankFileName).isEqualTo("N/A");
    }

    @Test
    public void getBankFileName_forExteriorModel() {
        // GIVEN
        String slotReference = "11111111";
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        String resourceValue = "RX8";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 9, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(resourceValue));


        // WHEN
        String actualBankFileName = vehicleSlotsHelper.getBankFileName(slotReference, EXTERIOR_MODEL);


        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8.bnk");
    }

    @Test
    public void getBankFileName_forAudio() {
        // GIVEN
        String slotReference = "11111111";
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        String resourceValue = "RX8";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 9, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(resourceValue));


        // WHEN
        String actualBankFileName = vehicleSlotsHelper.getBankFileName(slotReference, SOUND);


        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_audio.bnk");
    }

    @Test
    public void getBankFileName_forInteriorModel() {
        // GIVEN
        String slotReference = "11111111";
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        String resourceValue = "RX8";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromContentEntry(1, 9, CAR_PHYSICS_DATA, UNITED_STATES)).thenReturn(of(resourceValue));


        // WHEN
        String actualBankFileName = vehicleSlotsHelper.getBankFileName(slotReference, INTERIOR_MODEL);


        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_I.bnk");
    }

    @Test
    public void getBankFileName_forFrontRimsModel() {
        // GIVEN
        String slotReference = "11111111";
        String rimSlotReference = "22222222";
        String resourceRef = "33333333";
        DbDataDto.Item physicsItem = DbDataDto.Item.builder()
                .ofFieldRank(10)
                .withRawValue(rimSlotReference)
                .build();
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .addItem(physicsItem)
                .build();
        DbDataDto.Item rimsItem = DbDataDto.Item.builder()
                .ofFieldRank(14)
                .withRawValue(resourceRef)
                .build();
        DbDataDto.Entry rimsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .addItem(rimsItem)
                .build();
        String rimsResourceValue = "RX8_F_01";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotReference, RIMS)).thenReturn(of(rimsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(resourceRef, RIMS, UNITED_STATES)).thenReturn(of(rimsResourceValue));


        // WHEN
        String actualBankFileName = vehicleSlotsHelper.getBankFileName(slotReference, FRONT_RIM);


        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_F_01.bnk");
    }

    @Test
    public void getBankFileName_forRearRimsModel() {
        // GIVEN
        String slotReference = "11111111";
        String rimSlotReference = "22222222";
        String resourceRef = "33333333";
        DbDataDto.Item physicsItem = DbDataDto.Item.builder()
                .ofFieldRank(10)
                .withRawValue(rimSlotReference)
                .build();
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .addItem(physicsItem)
                .build();
        DbDataDto.Item rimsItem = DbDataDto.Item.builder()
                .ofFieldRank(15)
                .withRawValue(resourceRef)
                .build();
        DbDataDto.Entry rimsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .addItem(rimsItem)
                .build();
        String rimsResourceValue = "RX8_R_01";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotReference, RIMS)).thenReturn(of(rimsEntry));
        when(bulkDatabaseMinerMock.getLocalizedResourceValueFromTopicAndReference(resourceRef, RIMS, UNITED_STATES)).thenReturn(of(rimsResourceValue));


        // WHEN
        String actualBankFileName = vehicleSlotsHelper.getBankFileName(slotReference, REAR_RIM);


        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_R_01.bnk");
    }

    @Test
    public void getVehicleIdentifier_whenSlotExists() {
        // GIVEN
        String slotReference = "11111111";
        DbDataDto.Item physicsItem = DbDataDto.Item.builder()
                .ofFieldRank(102)
                .withRawValue("1000")
                .build();
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .addItem(physicsItem)
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));


        // WHEN
        final Integer vehicleIdentifier = vehicleSlotsHelper.getVehicleIdentifier(slotReference);


        // THEN
        assertThat(vehicleIdentifier).isEqualTo(1000);
    }

    @Test
    public void getVehicleIdentifier_whenSlotDoesNotExist_shouldReturn0() {
        // GIVEN
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(anyString(), eq(CAR_PHYSICS_DATA))).thenReturn(empty());


        // WHEN
        final Integer vehicleIdentifier = vehicleSlotsHelper.getVehicleIdentifier("11111111");


        // THEN
        assertThat(vehicleIdentifier).isZero();
    }
}
