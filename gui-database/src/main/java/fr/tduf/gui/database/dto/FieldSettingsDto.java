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

    @JsonProperty("label")
    private String label;

    @JsonProperty("toolTip")
    private String toolTip;

    public FieldSettingsDto() {}

    public FieldSettingsDto(String fieldName) {
        this.name = fieldName;
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
}