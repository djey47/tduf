package fr.tduf.gui.common.helper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessagesHelperTest {

//    @BeforeAll
//    static void globalSetUp() {
//        ApplicationTestHelper.initJavaFX();
//    }

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
    @Disabled("No async way of testing")
    void getServiceErrorMessage() {
        // TODO Find a way to unit test (async issues)
        // given
//        CompletableFuture<String> serviceCompletionFuture = new CompletableFuture<>();
//        CompletableFuture<String> helperCompletionFuture = new CompletableFuture<>();
//        Service<String> service = new Service<String>() {
//            @Override
//            protected Task<String> createTask() {
//                return new Task<String>() {
//                    @Override
//                    protected String call() throws Exception {
//                        updateMessage("This is a service error");
//                        serviceCompletionFuture.complete("");
//                        return "";
//                    }
//                };
//            }
//        };

        // when
//        Platform.runLater(() -> {
//            service.start();
//            try {
//                if (serviceCompletionFuture.get() != null) {
//                    helperCompletionFuture.complete(MessagesHelper.getServiceErrorMessage(service));
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        });
//        String actual = helperCompletionFuture.get();
//        Log.info(actual);

        // then
//        assertThat(actual).isEqualTo("This is a service error\rCheck logs for details.");
    }
}
