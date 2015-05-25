package fr.tduf.gui.database.controllers;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import static java.util.Objects.requireNonNull;

public class ChangeDataController {
    private final MainStageController mainStageController;

    ChangeDataController(MainStageController mainStageController) {
        requireNonNull(mainStageController, "Main stage controller is required.");

        this.mainStageController = mainStageController;
    }

    void updateContentItem(int fieldRank, String newRawValue) {
        DbDto.Topic topic = this.mainStageController.getCurrentTopicObject().getTopic();
        DbDataDto.Item contentItem = this.getMiner().getContentItemFromEntryIdentifierAndFieldRank(topic, fieldRank, this.mainStageController.getCurrentEntryIndex()).get();

        if (!contentItem.getRawValue().equals(newRawValue)) {
            contentItem.setRawValue(newRawValue);

            this.mainStageController.getViewDataController().updateItemProperties(contentItem);
        }
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getDatabaseMiner();
    }
}