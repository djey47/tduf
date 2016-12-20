package fr.tduf.gui.database.controllers.helper;

import fr.tduf.gui.database.controllers.MainStageController;
import fr.tduf.gui.database.controllers.MainStageViewDataController;
import fr.tduf.gui.database.dto.EditorLayoutDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.INTEGER;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DynamicFieldControlsHelperTest {
    @Mock
    private MainStageController controller;

    @Mock
    private MainStageViewDataController viewDataMock;

    @InjectMocks
    private DynamicFieldControlsHelper helper;


    @Test
    public void addAllFieldsControls_whenNoFieldSettings_shouldDoNothing() throws Exception {
        // GIVEN
        String profileName = "Profile 1";
        EditorLayoutDto layout = new EditorLayoutDto();
        EditorLayoutDto.EditorProfileDto profile = new EditorLayoutDto.EditorProfileDto(profileName);
        layout.getProfiles().add(profile);

        DbDto currentTopicObject = DbDto.builder()
                .withStructure(DbStructureDto.builder().addItem(
                        DbStructureDto.Field.builder()
                                .ofRank(1)
                                .fromType(INTEGER)
                                .build())
                        .build())
                .build();

        when(controller.getCurrentTopicObject()).thenReturn(currentTopicObject);
        when(controller.getLayoutObject()).thenReturn(layout);
        when(controller.getViewData()).thenReturn(viewDataMock);
        when(viewDataMock.currentProfile()).thenReturn(new SimpleObjectProperty<>(profile));


        // WHEN-THEN
        helper.addAllFieldsControls(layout, profileName, DbDto.Topic.CAR_PHYSICS_DATA);
    }
}
