package fr.tduf.gui.launcher.domain.javafx;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class GameSettingDataItem {
    private final Property<String> name = new SimpleStringProperty();
    private final Property<Boolean> enabled = new SimpleBooleanProperty();

    public GameSettingDataItem(String name) {
        this.name.setValue(name);
    }

    public Property<String> getNameProperty() {
        return name;
    }

    public Property<Boolean> getEnabledProperty() {
        return enabled;
    }
}
