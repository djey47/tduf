package fr.tduf.gui.installer.common.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.installer.common.helper.VehicleSlotsHelper.BankFileType.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.RIMS;
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
        DbResourceDto.Entry resourceEntry = DbResourceDto.Entry.builder()
                .forReference("RES")
                .withValue(realName)
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 12, 1, UNITED_STATES)).thenReturn(of(resourceEntry));


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
        DbResourceDto.Entry resourceEntry = DbResourceDto.Entry.builder()
                .withValue("??")
                .build();
        DbResourceDto.Entry modelResourceEntry = DbResourceDto.Entry.builder()
                .withValue(modelName)
                .build();
        DbResourceDto.Entry versionResourceEntry = DbResourceDto.Entry.builder()
                .withValue(versionName)
                .build();

        DbDataDto.Entry remoteContentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbResourceDto.Entry remoteBrandsResourceEntry = DbResourceDto.Entry.builder()
                .withValue(brandName)
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 12, 1, UNITED_STATES)).thenReturn(of(resourceEntry));
        when(bulkDatabaseMinerMock.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, 2, 1, BRANDS)).thenReturn(of(remoteContentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(BRANDS, 3, 1, UNITED_STATES)).thenReturn(of(remoteBrandsResourceEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 13, 1, UNITED_STATES)).thenReturn(of(modelResourceEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 14, 1, UNITED_STATES)).thenReturn(of(versionResourceEntry));


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
        DbResourceDto.Entry resourceEntry = DbResourceDto.Entry.builder()
                .withValue("??")
                .build();
        DbResourceDto.Entry modelResourceEntry = DbResourceDto.Entry.builder()
                .withValue(modelName)
                .build();

        DbDataDto.Entry remoteContentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 12, 1, UNITED_STATES)).thenReturn(of(resourceEntry));
        when(bulkDatabaseMinerMock.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, 2, 1, BRANDS)).thenReturn(of(remoteContentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(BRANDS, 3, 1, UNITED_STATES)).thenReturn(Optional.empty());
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 13, 1, UNITED_STATES)).thenReturn(of(modelResourceEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 14, 1, UNITED_STATES)).thenReturn(Optional.empty());


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
        DbResourceDto.Entry resourceEntry = DbResourceDto.Entry.builder()
                .withValue("??")
                .build();
        DbResourceDto.Entry modelResourceEntry = DbResourceDto.Entry.builder()
                .withValue(modelName)
                .build();

        DbDataDto.Entry remoteContentEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbResourceDto.Entry remoteBrandsResourceEntry = DbResourceDto.Entry.builder()
                .withValue(brandName)
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(contentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 12, 1, UNITED_STATES)).thenReturn(of(resourceEntry));
        when(bulkDatabaseMinerMock.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, 2, 1, BRANDS)).thenReturn(of(remoteContentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(BRANDS, 3, 1, UNITED_STATES)).thenReturn(of(remoteBrandsResourceEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 13, 1, UNITED_STATES)).thenReturn(of(modelResourceEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 14, 1, UNITED_STATES)).thenReturn(Optional.empty());


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
        String rimResourceRef = "33333333";
        long entryId = 1;
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(entryId)
                .build();
        DbDataDto.Entry rimsEntry = DbDataDto.Entry.builder()
                .forId(entryId)
                .build();
        DbDataDto.Item physicsItem = DbDataDto.Item.builder()
                .ofFieldRank(10)
                .withRawValue(rimRef)
                .build();
        DbResourceDto.Entry rimsResourceEntry = DbResourceDto.Entry.builder()
                .forReference(rimResourceRef)
                .withValue("Toyota")
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentItemWithEntryIdentifierAndFieldRank(CAR_PHYSICS_DATA, 10, entryId)).thenReturn(of(physicsItem));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimRef, RIMS)).thenReturn(of(rimsEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(RIMS, 13, entryId, UNITED_STATES)).thenReturn(of(rimsResourceEntry));


        // WHEN
        String actualRimDirectory = vehicleSlotsHelper.getDefaultRimDirectoryForVehicle(slotReference);


        // THEN
        assertThat(actualRimDirectory).isEqualTo("Toyota");
    }

    @Test
    public void getDefaultRimDirectoryForVehicle_whenSlotDoesNotExist_shouldReturnEmptyString() {
        // GIVEN
        String slotReference = "11111111";

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(empty());


        // WHEN
        String actualRimDirectory = vehicleSlotsHelper.getDefaultRimDirectoryForVehicle(slotReference);


        // THEN
        assertThat(actualRimDirectory).isEmpty();
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
        String resourceReference = "22222222";
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbResourceDto.Entry resourceEntry = DbResourceDto.Entry.builder()
                .forReference(resourceReference)
                .withValue("RX8")
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 9, 1, UNITED_STATES)).thenReturn(of(resourceEntry));


        // WHEN
        String actualBankFileName = vehicleSlotsHelper.getBankFileName(slotReference, EXTERIOR_MODEL);


        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8.bnk");
    }

    @Test
    public void getBankFileName_forAudio() {
        // GIVEN
        String slotReference = "11111111";
        String resourceReference = "22222222";
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbResourceDto.Entry resourceEntry = DbResourceDto.Entry.builder()
                .forReference(resourceReference)
                .withValue("RX8")
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 9, 1, UNITED_STATES)).thenReturn(of(resourceEntry));


        // WHEN
        String actualBankFileName = vehicleSlotsHelper.getBankFileName(slotReference, SOUND);


        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_audio.bnk");
    }

    @Test
    public void getBankFileName_forInteriorModel() {
        // GIVEN
        String slotReference = "11111111";
        String resourceReference = "22222222";
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbResourceDto.Entry resourceEntry = DbResourceDto.Entry.builder()
                .forReference(resourceReference)
                .withValue("RX8")
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 9, 1, UNITED_STATES)).thenReturn(of(resourceEntry));


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
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbDataDto.Item physicsItem = DbDataDto.Item.builder()
                .ofFieldRank(10)
                .withRawValue(rimSlotReference)
                .build();
        DbDataDto.Entry rimsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbResourceDto.Entry rimsResourceEntry = DbResourceDto.Entry.builder()
                .withValue("RX8_F_01")
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentItemWithEntryIdentifierAndFieldRank(CAR_PHYSICS_DATA, 10, 1)).thenReturn(of(physicsItem));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotReference, RIMS)).thenReturn(of(rimsEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(RIMS, 14, 1, UNITED_STATES)).thenReturn(of(rimsResourceEntry));


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
        DbDataDto.Entry physicsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbDataDto.Item physicsItem = DbDataDto.Item.builder()
                .ofFieldRank(10)
                .withRawValue(rimSlotReference)
                .build();
        DbDataDto.Entry rimsEntry = DbDataDto.Entry.builder()
                .forId(1)
                .build();
        DbResourceDto.Entry rimsResourceEntry = DbResourceDto.Entry.builder()
                .withValue("RX8_R_01")
                .build();

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(of(physicsEntry));
        when(bulkDatabaseMinerMock.getContentItemWithEntryIdentifierAndFieldRank(CAR_PHYSICS_DATA, 10, 1)).thenReturn(of(physicsItem));
        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(rimSlotReference, RIMS)).thenReturn(of(rimsEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(RIMS, 15, 1, UNITED_STATES)).thenReturn(of(rimsResourceEntry));


        // WHEN
        String actualBankFileName = vehicleSlotsHelper.getBankFileName(slotReference, REAR_RIM);


        // THEN
        assertThat(actualBankFileName).isEqualTo("RX8_R_01.bnk");
    }
}
