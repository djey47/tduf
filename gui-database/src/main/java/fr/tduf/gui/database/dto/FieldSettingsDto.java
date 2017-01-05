package fr.tduf.gui.database.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

@JsonTypeName("fieldSettings")
public class FieldSettingsDto {
    @JsonProperty("rank")
    private int rank;

    @JsonProperty("name")
    private String name;

    @JsonProperty("priority")
    private int priority;

    @JsonProperty("readOnly")
    private boolean readOnly;

    @JsonProperty("hidden")
    private boolean hidden;

    @JsonProperty("label")
    private String label;

    @JsonProperty("toolTip")
    private String toolTip;

    @JsonProperty("group")
    private String group;

    @JsonProperty("remoteReferenceProfile")
    private String remoteReferenceProfile;

    @JsonProperty("pluginName")
    private String pluginName;

    public FieldSettingsDto() {}

    public FieldSettingsDto(int fieldRank) {
        this.rank = fieldRank;
    }

    public String getLabel() {
        return label;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getGroup() {
        return group;
    }

    public String getToolTip() {
        return toolTip;
    }

    public int getPriority() {
        return priority;
    }

    public String getRemoteReferenceProfile() {
        return remoteReferenceProfile;
    }

    public void setRemoteReferenceProfile(String remoteReferenceProfile) {
        this.remoteReferenceProfile = remoteReferenceProfile;
    }
    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getPluginName() {
        return pluginName;
    }
}
