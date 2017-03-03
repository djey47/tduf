package fr.tduf.gui.database.plugins.mapping.domain;

/**
 * Model for mapping table row
 */
public class MappingEntry {
    private String kind;
    private String path;

    private boolean exists;
    private boolean registered;

    public MappingEntry(String kind, String path, boolean exists, boolean registered) {
        this.kind = kind;
        this.path = path;
        this.exists = exists;
        this.registered = registered;
    }
    
    public String getPath() {
        return path;
    }

    public boolean isExists() {
        return exists;
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getKind() {
        return kind;
    }
}
