package fr.tduf.gui.database.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

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

    @JsonProperty("remoteFieldRanks")
    private List<Integer> remoteFieldRanks = new ArrayList<>();

    @JsonProperty("remoteReferenceProfile")
    private String remoteReferenceProfile;

    public FieldSettingsDto() {}

    public FieldSettingsDto(String fieldName) {
        this.name = fieldName;
    }

    public FieldSettingsDto(int fieldRank) {
        this.rank = fieldRank;
    }

    public String getName() {
        return name;
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

    public List<Integer> getRemoteFieldRanks() {
        return remoteFieldRanks;
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

    public int getRank() {
        return rank;
    }
}