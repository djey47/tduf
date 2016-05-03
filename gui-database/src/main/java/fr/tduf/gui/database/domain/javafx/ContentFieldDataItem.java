package fr.tduf.gui.database.domain.javafx;

import com.google.common.base.MoreObjects;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a key-value pair to be displayed in a TableView.
 * Only applies to a content entry.
 * Also includes database entry identifier (optional).
 */
public class ContentFieldDataItem {
    private IntegerProperty rank = new SimpleIntegerProperty();

    private StringProperty name = new SimpleStringProperty();

    private StringProperty help = new SimpleStringProperty();

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
        return MoreObjects.toStringHelper(this)
                .add("rank", rank)
                .add("name", name)
                .add("help", help)
                .toString();
    }
}
