package fr.tduf.gui.common.javafx.helper.options;

import javafx.stage.FileChooser;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifies file selector appearance and behaviour.
 */
public class FileBrowsingOptions {
    public enum FileBrowsingContext { LOAD, SAVE }

    public static final FileBrowsingOptions defaultSettings = new FileBrowsingOptions();

    private FileBrowsingOptions() {}

    protected final List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
    protected String dialogTitle = "";
    protected FileBrowsingContext context = FileBrowsingContext.LOAD;
    protected String initialDirectory = ".";

    /**
     * @return instance creator
     */
    public static FileBrowsingOptionsBuilder builder() {
        return new FileBrowsingOptionsBuilder();
    }

    public List<FileChooser.ExtensionFilter> getExtensionFilters() {
        return extensionFilters;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public FileBrowsingContext getContext() {
        return context;
    }

    public String getInitialDirectory() {
        return initialDirectory;
    }

    public static class FileBrowsingOptionsBuilder extends FileBrowsingOptions {
        public FileBrowsingOptionsBuilder forLoading() {
            context = FileBrowsingContext.LOAD;
            return this;
        }

        public FileBrowsingOptionsBuilder forSaving() {
            context = FileBrowsingContext.SAVE;
            return this;
        }

        public FileBrowsingOptionsBuilder withDialogTitle(String title) {
            dialogTitle = title;
            return this;
        }

        public FileBrowsingOptionsBuilder withExtensionFilters(List<FileChooser.ExtensionFilter> extensionFilters) {
            this.extensionFilters.clear();
            this.extensionFilters.addAll(extensionFilters);
            return this;
        }

        public FileBrowsingOptionsBuilder withInitialDirectory(String initialDirectory) {
            this.initialDirectory = initialDirectory;
            return this;
        }

        public FileBrowsingOptions build() {
            FileBrowsingOptions fileBrowsingOptions = new FileBrowsingOptions();

            fileBrowsingOptions.context = context;
            fileBrowsingOptions.extensionFilters.addAll(extensionFilters);
            fileBrowsingOptions.dialogTitle = dialogTitle;
            fileBrowsingOptions.initialDirectory = initialDirectory;

            return fileBrowsingOptions;
        }
    }
}
