package fr.tduf.gui.database.plugins.common.contexts;

import fr.tduf.gui.database.plugins.mapping.domain.MappingEntry;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

/**
 * Contains on-the-fly context for correct plugin execution. Can be extended to provide specific information.
 */
public class OnTheFlyContext {
    private DbDto.Topic currentTopic;
    private DbDto.Topic remoteTopic;

    private int fieldRank;
    private boolean fieldReadOnly;

    private Pane parentPane;

    private Property<Integer> contentEntryIndexProperty;
    private StringProperty rawValueProperty;
    private StringProperty errorMessageProperty;
    private BooleanProperty errorProperty;

    // TODO See to move to mapping context
    private ObservableList<MappingEntry> files;

    public DbDto.Topic getCurrentTopic() {
        return currentTopic;
    }

    public void setCurrentTopic(DbDto.Topic currentTopic) {
        this.currentTopic = currentTopic;
    }

    public int getFieldRank() {
        return fieldRank;
    }

    public void setFieldRank(int fieldRank) {
        this.fieldRank = fieldRank;
    }

    public boolean isFieldReadOnly() {
        return fieldReadOnly;
    }

    public StringProperty getRawValueProperty() {
        return rawValueProperty;
    }

    public void setFieldReadOnly(boolean fieldReadOnly) {
        this.fieldReadOnly = fieldReadOnly;
    }

    public void setRawValueProperty(StringProperty rawValueProperty) {
        this.rawValueProperty = rawValueProperty;
    }

    public StringProperty getErrorMessageProperty() {
        return errorMessageProperty;
    }

    public void setErrorMessageProperty(StringProperty errorMessageProperty) {
        this.errorMessageProperty = errorMessageProperty;
    }

    public BooleanProperty getErrorProperty() {
        return errorProperty;
    }

    public void setErrorProperty(BooleanProperty errorProperty) {
        this.errorProperty = errorProperty;
    }

    public DbDto.Topic getRemoteTopic() {
        return remoteTopic;
    }

    public void setRemoteTopic(DbDto.Topic remoteTopic) {
        this.remoteTopic = remoteTopic;
    }

    public Property<Integer> getContentEntryIndexProperty() {
        return contentEntryIndexProperty;
    }

    public void setContentEntryIndexProperty(Property<Integer> contentEntryIndexProperty) {
        this.contentEntryIndexProperty = contentEntryIndexProperty;
    }

    public void setFiles(ObservableList<MappingEntry> files) {
        this.files = files;
    }

    public ObservableList<MappingEntry> getFiles() {
        return files;
    }

    public Pane getParentPane() {
        return parentPane;
    }

    public void setParentPane(Pane parentPane) {
        this.parentPane = parentPane;
    }
}
