package fr.tduf.gui.database.converter;

import fr.tduf.gui.database.domain.javafx.ContentEntryDataItem;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentEntryToStringConverterTest {
    private ContentEntryToStringConverter contentEntryToStringConverter;

    @BeforeEach
    void setUp() {
        ObservableList<ContentEntryDataItem> browsableEntries = FXCollections.observableArrayList();
        Property<Integer> currentEntryIndexProperty = new SimpleObjectProperty<>(1);
        StringProperty currentEntryLabelProperty = new SimpleStringProperty("ITEM VALUE ??");

        contentEntryToStringConverter = new ContentEntryToStringConverter(browsableEntries, currentEntryIndexProperty, currentEntryLabelProperty);
        contentEntryToStringConverter.getBrowsableEntries().clear();
    }

    @Test
    void toString_whenProvidedItem() {
        // given
        ContentEntryDataItem item = new ContentEntryDataItem();

        // when
        String actual = contentEntryToStringConverter.toString(item);

        // then
        assertThat(actual).isEqualTo("2: ITEM VALUE _");
    }

    @Test
    void toString_whenNullItem_shouldReturnNull() {
        // given-when-then
        assertThat(contentEntryToStringConverter.toString(null)).isNull();
    }

    @Test
    void fromString_shouldReturnItemAtCurrentIndex() {
        // given
        ContentEntryDataItem firstItem = new ContentEntryDataItem();
        ContentEntryDataItem expectedItem = new ContentEntryDataItem();
        contentEntryToStringConverter.getBrowsableEntries().addAll(firstItem, expectedItem);

        // when
        ContentEntryDataItem actualItem = contentEntryToStringConverter.fromString("2: ITEM VALUE _");

        // then
        assertThat(actualItem).isSameAs(expectedItem);
    }

    @Test
    void fromString_whenNullLabel_shouldReturnNull() {
        // given-when-then
        assertThat(contentEntryToStringConverter.fromString(null)).isNull();
    }

    @Test
    void getLabelFromEntry_whenValidEntryItem() {
        // given
        ContentEntryDataItem item = new ContentEntryDataItem();
        item.setInternalEntryId(0);
        item.setValue("ITEM VALUE ??");

        // when
        String actual = ContentEntryToStringConverter.getLabelFromEntry(item);

        // then
        assertThat(actual).isEqualTo("1: ITEM VALUE _");
    }
}
