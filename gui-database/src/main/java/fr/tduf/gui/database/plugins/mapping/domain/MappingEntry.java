package fr.tduf.gui.database.plugins.mapping.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model for mapping table row
 */
public class MappingEntry {
    private final StringProperty kind = new SimpleStringProperty("");
    private final StringProperty path = new SimpleStringProperty("");

    private final BooleanProperty exists = new SimpleBooleanProperty(false);
    private final BooleanProperty registered = new SimpleBooleanProperty(false);

    public MappingEntry(String kind, String path, boolean exists, boolean registered) {
        this.kind.setValue(kind);
        this.path.setValue(path);
        this.exists.set(exists);
        this.registered.set(registered);
    }
    
    public String getPath() {
        return path.getValue();
    }

    public boolean isExists() {
        return exists.get();
    }

    public BooleanProperty existingProperty() {
        return exists;
    }    
    
    public BooleanProperty registeredProperty() {
        return registered;
    }

    public boolean isRegistered() {
        return registered.get();
    }
    
    public void registered() {
        registered.set(true);
    }

    public String getKind() {
        return kind.getValue();
    }
}
