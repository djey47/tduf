package fr.tduf.gui.common.services.tasks;

import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * To be extended by any Service task, to get benefits of message history and more...
 * @param <V>
 */
public abstract class GenericServiceTask<V> extends Task<V> {
    private final List<String> messageHistory = new ArrayList<>();

    @Override
    protected void updateMessage(String s) {
        requireNonNull(s, "Message cannot be null");

        messageHistory.add(s);
        super.updateMessage(s);
    }

    public List<String> getMessageHistory() {
        return messageHistory;
    }
}
