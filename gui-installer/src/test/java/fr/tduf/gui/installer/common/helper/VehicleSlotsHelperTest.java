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

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.UNITED_STATES;
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

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(Optional.of(contentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 12, 1, UNITED_STATES)).thenReturn(Optional.of(resourceEntry));


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

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(Optional.of(contentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 12, 1, UNITED_STATES)).thenReturn(Optional.of(resourceEntry));
        when(bulkDatabaseMinerMock.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, 2, 1, BRANDS)).thenReturn(Optional.of(remoteContentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(BRANDS, 3, 1, UNITED_STATES)).thenReturn(Optional.of(remoteBrandsResourceEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 13, 1, UNITED_STATES)).thenReturn(Optional.of(modelResourceEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 14, 1, UNITED_STATES)).thenReturn(Optional.of(versionResourceEntry));


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

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(Optional.of(contentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 12, 1, UNITED_STATES)).thenReturn(Optional.of(resourceEntry));
        when(bulkDatabaseMinerMock.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, 2, 1, BRANDS)).thenReturn(Optional.of(remoteContentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(BRANDS, 3, 1, UNITED_STATES)).thenReturn(Optional.empty());
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 13, 1, UNITED_STATES)).thenReturn(Optional.of(modelResourceEntry));
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

        when(bulkDatabaseMinerMock.getContentEntryFromTopicWithReference(slotReference, CAR_PHYSICS_DATA)).thenReturn(Optional.of(contentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 12, 1, UNITED_STATES)).thenReturn(Optional.of(resourceEntry));
        when(bulkDatabaseMinerMock.getRemoteContentEntryWithInternalIdentifier(CAR_PHYSICS_DATA, 2, 1, BRANDS)).thenReturn(Optional.of(remoteContentEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(BRANDS, 3, 1, UNITED_STATES)).thenReturn(Optional.of(remoteBrandsResourceEntry));
        when(bulkDatabaseMinerMock.getResourceEntryWithContentEntryInternalIdentifier(CAR_PHYSICS_DATA, 13, 1, UNITED_STATES)).thenReturn(Optional.of(modelResourceEntry));
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

        when(bulkDatabaseMinerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(Optional.of(topicObject));


        // WHEN
        final List<DbDataDto.Entry> actualEntries = vehicleSlotsHelper.getDrivableVehicleSlotEntries();


        // THEN
        assertThat(actualEntries).hasSize(1);
        assertThat(actualEntries.get(0).getItemAtRank(1).get().getRawValue()).isEqualTo(drivableRef);
    }
}
