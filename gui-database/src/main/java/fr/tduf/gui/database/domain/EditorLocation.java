package fr.tduf.gui.database.domain;

/**
 * Represents a particular location in GUI.
 */
public class EditorLocation {
    private String profileName;

    private long entryId;

    private int tabId;

    public EditorLocation(int tabId, String profileName, long entryId) {
        this.tabId = tabId;
        this.profileName = profileName;
        this.entryId = entryId;
    }

    public String getProfileName() {
        return profileName;
    }

    public long getEntryId() {
        return entryId;
    }

    public int getTabId() {
        return tabId;
    }
}