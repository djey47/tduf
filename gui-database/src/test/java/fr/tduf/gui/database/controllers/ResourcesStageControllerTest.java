package fr.tduf.gui.database.controllers;


import fr.tduf.gui.database.domain.LocalizedResource;
import fr.tduf.libtesting.common.helper.javafx.JavaFXThreadingRule;
import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static fr.tduf.libunlimited.common.game.domain.Locale.FRANCE;
import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
        topicsChoiceBox.setValue(CAR_PHYSICS_DATA);

        controller.setTopicsChoiceBox(topicsChoiceBox);
        controller.setMainStageController(mainStageControllerMock);

        when(mainStageControllerMock.getMiner()).thenReturn(minerMock);
        when(mainStageControllerMock.getViewData()).thenReturn(viewDataControllerMock);
        when(mainStageControllerMock.getChangeData()).thenReturn(changeDataControllerMock);

        when(minerMock.getResourcesFromTopic(CAR_PHYSICS_DATA)).thenReturn(empty());
    }

    @Test
    @Ignore("Displays message box so can't be run automatically")
    public void editResourceAndUpdateMainStage_whenResourceDoesNotExist_shouldRaisePopup() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("0", "V"), of(FRANCE));
        doThrow(new IllegalArgumentException("Does not exist"))
                .when(changeDataControllerMock).updateResourceWithReferenceForLocale(CAR_PHYSICS_DATA, FRANCE, "0", "V");

        // WHEN
        controller.editResourceAndUpdateMainStage(CAR_PHYSICS_DATA, "0", newLocalizedResource);

        // THEN: manual assertions
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    public void editResourceAndUpdateMainStage_whenNewValueForLocale_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("0", "V"), of(FRANCE));

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

        // WHEN
        controller.editResourceAndUpdateMainStage(CAR_PHYSICS_DATA, "0", newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).updateResourceWithReferenceForAllLocales(CAR_PHYSICS_DATA, "0", "V");
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    public void editResourceAndUpdateMainStage_whenExistingResourceReference_andNewValueAndReference_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("1", "V"), empty());

        // WHEN
        controller.editResourceAndUpdateMainStage(CAR_PHYSICS_DATA, "0", newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).updateResourceWithReferenceForAllLocales(CAR_PHYSICS_DATA, "0", "1", "V");
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    @Ignore("Displays message box so can't be run automatically")
    public void editNewResourceAndUpdateMainStage_whenResourceAlreadyExists_shouldRaisePopup() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("0", "V"), of(FRANCE));
        doThrow(new IllegalArgumentException("Already exists"))
                .when(changeDataControllerMock).addResourceWithReference(CAR_PHYSICS_DATA, FRANCE, "0", "V");

        // WHEN
        controller.editNewResourceAndUpdateMainStage(CAR_PHYSICS_DATA, newLocalizedResource);

        // THEN: manual assertions
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    public void editNewResourceAndUpdateMainStage_forAllLocales_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("1", "V"), empty());

        // WHEN
        controller.editNewResourceAndUpdateMainStage(CAR_PHYSICS_DATA, newLocalizedResource);

        // THEN
        verify(changeDataControllerMock, times(8)).addResourceWithReference(eq(CAR_PHYSICS_DATA), any(Locale.class), eq("1"), eq("V"));
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    public void editNewResourceAndUpdateMainStage_forSingleLocale_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("1", "V"), of(UNITED_STATES));

        // WHEN
        controller.editNewResourceAndUpdateMainStage(CAR_PHYSICS_DATA, newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).addResourceWithReference(eq(CAR_PHYSICS_DATA), eq(UNITED_STATES), eq("1"), eq("V"));
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }
}
