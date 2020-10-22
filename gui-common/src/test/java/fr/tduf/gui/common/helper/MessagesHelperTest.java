package fr.tduf.gui.common.helper;

import javafx.concurrent.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MessagesHelperTest {

    /**
     * Requires MockMaker extension for mockito
     */
    @Mock
    private Service<Void> mockService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void getGenericErrorMessage() {
        // given
        Exception exception = new Exception("This is an exception");

        // when
        String actual = MessagesHelper.getGenericErrorMessage(exception);

        // then
        assertThat(actual).isEqualTo("This is an exception\nCheck logs for details.");
    }

    @Test
    void getGenericErrorMessage_withoutAdditionalMessage_shouldWriteMessageAndCauseMessage() {
        // given
        Throwable throwable = new Throwable("This is a throwable", new Throwable("This is the cause"));

        // when
        String actual = MessagesHelper.getAdvancedErrorMessage(throwable, null);

        // then
        assertThat(actual).isEqualTo("This is a throwable\nThis is the cause\n\nCheck logs for details.");
    }

    @Test
    void getGenericErrorMessage_withAdditionalMessage_shouldWriteMessages() {
        // given
        Throwable throwable = new Throwable("This is a throwable");
        String additionalMessage = "Additional message";

        // when
        String actual = MessagesHelper.getAdvancedErrorMessage(throwable, additionalMessage);

        // then
        assertThat(actual).isEqualTo("This is a throwable\n\nAdditional message\nCheck logs for details.");
    }

    @Test
    void getServiceErrorMessage() {
        // given
        when(mockService.getMessage()).thenReturn("This is a service error");

        // when
        String actualMessage = MessagesHelper.getServiceErrorMessage(mockService);

        // then
        assertThat(actualMessage).isEqualTo("This is a service error\nCheck logs for details.");
    }
}
