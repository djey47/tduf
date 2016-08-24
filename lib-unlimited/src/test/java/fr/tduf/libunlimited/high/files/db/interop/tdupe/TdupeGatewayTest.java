package fr.tduf.libunlimited.high.files.db.interop.tdupe;

import fr.tduf.libtesting.common.helper.game.DatabaseHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.high.files.db.patcher.DatabasePatcher;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TdupeGatewayTest {
    private static final Class<TdupeGatewayTest> thisClass = TdupeGatewayTest.class;

    @Mock
    private BulkDatabaseMiner minerMock;

    @Mock
    private DatabasePatcher patcherMock;

    @InjectMocks
    private TdupeGateway gateway;

    @Test
    public void applyPerformancePackToEntryWithIdentifier_whenEntryExists_shouldInvokeMinerAndPatcher() throws ReflectiveOperationException, IOException, URISyntaxException {
        // GIVEN
        String performancePackFile = thisClass.getResource("/db/patch/tdupe/F150.tdupk").getFile();

        DbDto carPhysicsDataTopicObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);
        when(minerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(Optional.of(carPhysicsDataTopicObject));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, CAR_PHYSICS_DATA)).thenReturn(Optional.of("000000"));


        // WHEN
        gateway.applyPerformancePackToEntryWithIdentifier(0, performancePackFile);


        // THEN
        verify(patcherMock).apply(any());
    }
    @Test
    public void applyPerformancePackToEntryWithIdentifier_whenEntryDoesNotExist_shouldInvokeMinerAndPatcher() throws ReflectiveOperationException, IOException, URISyntaxException {
        // GIVEN
        String performancePackFile = thisClass.getResource("/db/patch/tdupe/F150.tdupk").getFile();

        DbDto carPhysicsDataTopicObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);
        when(minerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(Optional.of(carPhysicsDataTopicObject));
        when(minerMock.getContentEntryReferenceWithInternalIdentifier(0, CAR_PHYSICS_DATA)).thenReturn(Optional.empty());


        // WHEN
        gateway.applyPerformancePackToEntryWithIdentifier(0, performancePackFile);


        // THEN
        verify(patcherMock).apply(any());
    }

    @Test
    public void applyPerformancePackToEntryWithReference_shouldInvokeMinerAndPatcher() throws ReflectiveOperationException, IOException, URISyntaxException {
        // GIVEN
        String performancePackFile = thisClass.getResource("/db/patch/tdupe/F150.tdupk").getFile();

        DbDto carPhysicsDataTopicObject = DatabaseHelper.createDatabaseTopicForReadOnly(CAR_PHYSICS_DATA);
        when(minerMock.getDatabaseTopic(CAR_PHYSICS_DATA)).thenReturn(Optional.of(carPhysicsDataTopicObject));


        // WHEN
        gateway.applyPerformancePackToEntryWithReference(Optional.of("000000"), performancePackFile);


        // THEN
        verify(minerMock, never()).getContentEntryReferenceWithInternalIdentifier(anyLong(), any());
        verify(patcherMock).apply(any());
    }

    @Test(expected = NullPointerException.class)
    public void checkCarPhysicsDataLine_whenNullLine_shouldThrowException(){
        // GIVEN-WHEN
        TdupeGateway.checkCarPhysicsDataLine(null);

        // THEN:NPE
    }

    @Test(expected = RuntimeException.class)
    public void checkCarPhysicsDataLine_whenIncorrectLine_shouldThrowException() {
        // GIVEN-WHEN
        TdupeGateway.checkCarPhysicsDataLine("125654;1234456;1254");

        // THEN:RE
    }

    @Test
    public void checkCarPhysicsDataLine_whenCorrectLine_shouldNotThrowException() {
        // GIVEN-WHEN
        TdupeGateway.checkCarPhysicsDataLine("1139121456;77060;60733407;59733427;78900264;1;43055;59368917;60733407;1292203257;60733407;53338427;57356917;57356917;543629;553709;6210;411;5500;588;4500;6500;54356917;563531;6;4,10;4,17;2,34;1,52;1,14;0,86;0,69;0;2;5355936;350;347;5355936;61338424;0;1996;5603;1993;1869;1869;3385;965;1253;2767;500;500;380;480;130;160;95;95;95;95;105;30;52;0;0,380;2,5;45;50;60;1,4;1,4;1,7;2900;1;74739427;92261406;59533427;5566317;7,2;0;161;0;0;0;0;0;0;0;980;98;60;113;82,1;50;100;75;-1;1;231;237;1;100;237;79;");

        // THEN:no exception
    }
}
