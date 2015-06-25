package fr.tduf.gui.database.domain;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a resource with all values for locales, to be displayed in a TableView.
 */
// TODO simplify (3 classes for resources ...)
public class Resource {
    private StringProperty reference = new SimpleStringProperty();

    private Map<DbResourceDto.Locale, StringProperty> values = new HashMap<>();

    public StringProperty referenceProperty() {
        return reference;
    }

    public StringProperty valuePropertyForLocale(DbResourceDto.Locale locale) {
        createPropertyIfNotExists(locale);
        return values.get(locale);
    }

    private void createPropertyIfNotExists(DbResourceDto.Locale locale) {
        if (!this.values.containsKey(locale)) {
            this.values.put(locale, new SimpleStringProperty());
        }
    }

    /**
     * @return reference-value pair to be displayed for current entry.
     */
    public String toDisplayableValueForLocale(DbResourceDto.Locale locale) {
        return String.format(DisplayConstants.LABEL_ITEM_DATABASE_ENTRY, reference.get(), valuePropertyForLocale(locale).get());
    }


    public void setReference(String reference) {
        this.reference.set(reference);
    }

    public void setValueForLocale(DbResourceDto.Locale locale, String value) {
        createPropertyIfNotExists(locale);
        this.values.get(locale).set(value);
    }
}