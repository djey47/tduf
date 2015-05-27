package fr.tduf.gui.database.controllers;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

// TODO see to inherit main controller
public class MainStageChangeDataController {
    private final MainStageController mainStageController;

    MainStageChangeDataController(MainStageController mainStageController) {
        requireNonNull(mainStageController, "Main stage controller is required.");

        this.mainStageController = mainStageController;
    }

    void updateContentItem(DbDto.Topic topic, int fieldRank, String newRawValue) {
        DbDataDto.Item contentItem = this.getMiner().getContentItemFromEntryIdentifierAndFieldRank(topic, fieldRank, this.mainStageController.getCurrentEntryIndex()).get();

        if (!contentItem.getRawValue().equals(newRawValue)) {
            contentItem.setRawValue(newRawValue);

            // TODO see to update item properties automatically upon property change
            this.mainStageController.getViewDataController().updateItemProperties(contentItem);
        }
    }

    void removeResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference, boolean forAllLocales) {
        List<DbResourceDto.Locale> affectedLocales = singletonList(locale);
        if (forAllLocales) {
            affectedLocales = asList(DbResourceDto.Locale.values());
        }

        affectedLocales.stream()

                .map((affectedLocale) -> getMiner().getResourceFromTopicAndLocale(topic, locale).get().getEntries())

                .forEach((resources) -> {
                    Iterator<DbResourceDto.Entry> iterator = resources.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().getReference().equals(resourceReference)) {
                            iterator.remove();
                        }
                    }
                });
    }

    void updateResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String oldResourceReference, String newResourceReference, String newResourceValue) {
        DbResourceDto.Entry resourceEntry = getMiner().getResourceEntryFromTopicAndLocaleWithReference(oldResourceReference, topic, locale).get();

        resourceEntry.setReference(newResourceReference);
        resourceEntry.setValue(newResourceValue);
    }

    void addResourceWithReference(DbDto.Topic topic, DbResourceDto.Locale locale, String resourceReference, String resourceValue) {
        List<DbResourceDto.Entry> resourceEntries = getMiner().getResourceFromTopicAndLocale(topic, locale).get().getEntries();

        resourceEntries.add(DbResourceDto.Entry.builder()
                .forReference(resourceReference)
                .withValue(resourceValue)
                .build());
    }

    private BulkDatabaseMiner getMiner() {
        return this.mainStageController.getDatabaseMiner();
    }
}