package fr.tduf.gui.database.domain.javafx;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a key-value pair to be displayed in a TableView.
 * Only applies to a content entry.
 * Also includes database entry identifier (optional).
 */
public class ContentFieldDataItem {
    private final IntegerProperty rank = new SimpleIntegerProperty();

    private final StringProperty name = new SimpleStringProperty();

    private final StringProperty help = new SimpleStringProperty();

    public IntegerProperty rankProperty() {
        return rank;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty helpProperty() {
        return help;
    }

    public void setRank(int rank) {
        this.rank.set(rank);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setHelp(String help) {
        this.help.set(help);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
