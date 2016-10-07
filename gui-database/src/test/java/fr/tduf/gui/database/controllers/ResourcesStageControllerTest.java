package fr.tduf.gui.database.controllers;


import fr.tduf.gui.database.domain.LocalizedResource;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResourcesStageControllerTest {

    @Rule
    public JavaFXThreadingRule javaFXRule = new JavaFXThreadingRule();

    @Mock
    private MainStageController mainStageControllerMock;

    @Mock
    private MainStageViewDataController viewDataControllerMock;

    @Mock
    private MainStageChangeDataController changeDataControllerMock;

    @Mock
    private BulkDatabaseMiner minerMock;


    @InjectMocks
    private ResourcesStageController controller;

    private ChoiceBox<DbDto.Topic> topicsChoiceBox;

    @Before
    public void setUp() {
        topicsChoiceBox = new ChoiceBox<>();

        controller.setTopicsChoiceBox(topicsChoiceBox);
        controller.setMainStageController(mainStageControllerMock);

        when(mainStageControllerMock.getMiner()).thenReturn(minerMock);
        when(mainStageControllerMock.getViewData()).thenReturn(viewDataControllerMock);
        when(mainStageControllerMock.getChangeData()).thenReturn(changeDataControllerMock);
    }

    @Test
    public void editResourceAndUpdateMainStage_whenNewValueForLocale_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("0", "V"), of(FRANCE));
        topicsChoiceBox.setValue(CAR_PHYSICS_DATA);
        when(minerMock.getResourcesFromTopic(CAR_PHYSICS_DATA)).thenReturn(empty());

        // WHEN
        controller.editResourceAndUpdateMainStage(CAR_PHYSICS_DATA, "0", newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).updateResourceWithReferenceForLocale(CAR_PHYSICS_DATA, FRANCE, "0", "V");
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    public void editResourceAndUpdateMainStage_whenExistingResourceReference_andNewValueAnyLocale_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("0", "V"), empty());
        topicsChoiceBox.setValue(CAR_PHYSICS_DATA);
        when(minerMock.getResourcesFromTopic(CAR_PHYSICS_DATA)).thenReturn(empty());

        // WHEN
        controller.editResourceAndUpdateMainStage(CAR_PHYSICS_DATA, "0", newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).updateResourceWithReferenceForAllLocales(CAR_PHYSICS_DATA, "0", "V");
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }
}
