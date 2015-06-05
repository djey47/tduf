package fr.tduf.gui.database.converter;

import fr.tduf.gui.database.domain.RemoteResource;
import javafx.util.StringConverter;

public class RemoteResourceToStringConverter extends StringConverter<RemoteResource> {
    @Override
    public String toString(RemoteResource object) {
        if (object == null) {
            return "";
        }
        return object.valueProperty().get();
    }

    @Override
    public RemoteResource fromString(String string) {
        return null;
    }

}
