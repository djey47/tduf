package fr.tduf.gui.database.controllers;

import fr.tduf.gui.database.controllers.main.MainStageChangeDataController;
import fr.tduf.gui.database.controllers.main.MainStageController;
import fr.tduf.gui.database.controllers.main.MainStageViewDataController;
import fr.tduf.gui.database.domain.LocalizedResource;
import fr.tduf.libtesting.common.helper.javafx.ApplicationTestHelper;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.scene.control.ChoiceBox;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static fr.tduf.libunlimited.common.game.domain.Locale.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static java.util.Optional.empty;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class ResourcesStageControllerTest {
    @BeforeAll
    static void globalSetUp() {
        ApplicationTestHelper.initJavaFX();
    }

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

    @BeforeEach
    void setUp() {
        initMocks(this);
        
        ChoiceBox<DbDto.Topic> topicsChoiceBox = new ChoiceBox<>();
        topicsChoiceBox.setValue(CAR_PHYSICS_DATA);

        controller.setTopicsChoiceBox(topicsChoiceBox);
        controller.setMainStageController(mainStageControllerMock);

        when(mainStageControllerMock.getMiner()).thenReturn(minerMock);
        when(mainStageControllerMock.getViewData()).thenReturn(viewDataControllerMock);
        when(mainStageControllerMock.getChangeData()).thenReturn(changeDataControllerMock);

        when(minerMock.getResourcesFromTopic(CAR_PHYSICS_DATA)).thenReturn(empty());
    }

    @Test
    @Disabled("Displays message box so can't be run automatically")
    void editResourceAndUpdateMainStage_whenResourceDoesNotExist_shouldRaisePopup() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("0", "V"), FRANCE);
        doThrow(new IllegalArgumentException("Does not exist"))
                .when(changeDataControllerMock).updateResourceWithReferenceForLocale(CAR_PHYSICS_DATA, FRANCE, "0", "V");

        // WHEN
        controller.editResourceAndUpdateMainStage(CAR_PHYSICS_DATA, "0", newLocalizedResource);

        // THEN: manual assertions
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    void editResourceAndUpdateMainStage_whenNewValueForLocale_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("0", "V"), FRANCE);

        // WHEN
        controller.editResourceAndUpdateMainStage(CAR_PHYSICS_DATA, "0", newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).updateResourceWithReferenceForLocale(CAR_PHYSICS_DATA, FRANCE, "0", "V");
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    void editResourceAndUpdateMainStage_whenExistingResourceReference_andNewValueAnyLocale_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("0", "V"), null);

        // WHEN
        controller.editResourceAndUpdateMainStage(CAR_PHYSICS_DATA, "0", newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).updateResourceWithReferenceForAllLocales(CAR_PHYSICS_DATA, "0", "V");
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    void editResourceAndUpdateMainStage_whenExistingResourceReference_andNewValueAndReference_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("1", "V"), null);

        // WHEN
        controller.editResourceAndUpdateMainStage(CAR_PHYSICS_DATA, "0", newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).updateResourceWithReferenceForAllLocales(CAR_PHYSICS_DATA, "0", "1", "V");
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    @Disabled("Displays message box so can't be run automatically")
    void editNewResourceAndUpdateMainStage_whenResourceAlreadyExists_shouldRaisePopup() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("0", "V"), FRANCE);
        doThrow(new IllegalArgumentException("Already exists"))
                .when(changeDataControllerMock).addResourceWithReference(CAR_PHYSICS_DATA, FRANCE, "0", "V");

        // WHEN
        controller.editNewResourceAndUpdateMainStage(CAR_PHYSICS_DATA, newLocalizedResource);

        // THEN: manual assertions
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    void editNewResourceAndUpdateMainStage_forAllLocales_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("1", "V"), null);

        // WHEN
        controller.editNewResourceAndUpdateMainStage(CAR_PHYSICS_DATA, newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).addResourceWithReference(eq(CAR_PHYSICS_DATA), eq(DEFAULT), eq("1"), eq("V"));
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }

    @Test
    void editNewResourceAndUpdateMainStage_forSingleLocale_shouldCallChangeComponent_andUpdateAllStages() {
        // GIVEN
        LocalizedResource newLocalizedResource = new LocalizedResource(new Pair<>("1", "V"), UNITED_STATES);

        // WHEN
        controller.editNewResourceAndUpdateMainStage(CAR_PHYSICS_DATA, newLocalizedResource);

        // THEN
        verify(changeDataControllerMock).addResourceWithReference(eq(CAR_PHYSICS_DATA), eq(UNITED_STATES), eq("1"), eq("V"));
        verify(viewDataControllerMock).updateAllPropertiesWithItemValues();
    }
}
