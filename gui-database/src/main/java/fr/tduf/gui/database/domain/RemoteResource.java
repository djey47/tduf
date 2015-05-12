package fr.tduf.gui.database.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a key-value pair to be displayed in a TableView
 */
public class RemoteResource {
    private StringProperty reference = new SimpleStringProperty();

    private StringProperty value = new SimpleStringProperty();

    public StringProperty referenceProperty() {
        return reference;
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setReference(String reference) {
        this.reference.set(reference);
    }

    public void setValue(String value) {
        this.value.set(value);
    }
}
