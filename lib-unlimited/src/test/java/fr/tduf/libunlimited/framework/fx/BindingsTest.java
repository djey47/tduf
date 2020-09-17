package fr.tduf.libunlimited.framework.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class BindingsTest {
    @Mock
    ChangeListener<String> updateIntegerMock;

    @Mock
    ChangeListener<Integer> updateStringMock;

    private final StringProperty stringProperty = new SimpleStringProperty();
    private final ObjectProperty<Integer> integerProperty = new SimpleIntegerProperty().asObject();

    @BeforeEach
    void setUp() {
        initMocks(this);

        Bindings.bindBidirectional(
                stringProperty,
                integerProperty,
                updateIntegerMock,
                updateStringMock);
    }

    @Test
    void bindBidirectional_whenNoChange_shouldNotTriggerChangeListeners() {
        // given-when-then
        verifyNoMoreInteractions(updateIntegerMock, updateStringMock);
    }

    @Test
    void bindBidirectional_whenStringValueChanged_shouldTriggerIntegerChangeListenerOnce() {
        // given-when
        stringProperty.setValue("1");

        // then
        verifyNoMoreInteractions(updateStringMock);
        verify(updateIntegerMock, times(1)).changed(same(stringProperty), eq(null), eq("1"));
    }

    @Test
    void bindBidirectional_whenIntegerValueChanged_shouldTriggerStringChangeListenerOnce() {
        // given-when
        integerProperty.setValue(1);

        // then
        verifyNoMoreInteractions(updateIntegerMock);
        verify(updateStringMock, times(1)).changed(same(integerProperty), eq(0), eq(1));
    }

    @Test
    void bindBidirectional_whenIntegerValueChanged_shouldNotCreateChangeLoop_andStackOverflow() {
        // given
        stringProperty.unbind();
        integerProperty.unbind();
        Bindings.bindBidirectional(
                stringProperty,
                integerProperty,
                (observableValue, oldValue, newValue) -> integerProperty.setValue(Integer.parseInt(newValue) + 1),
                (observableValue, oldValue, newValue) -> stringProperty.setValue(Integer.toString(newValue + 1)));

        // when
        integerProperty.setValue(1);

        // then
        assertThat(stringProperty.get()).isEqualTo("2"); // integer(0 -> 1) => string(null -> "2")
        assertThat(integerProperty.get()).isEqualTo(3); // string(null -> "2") => integer(2 -> 3)
    }
}