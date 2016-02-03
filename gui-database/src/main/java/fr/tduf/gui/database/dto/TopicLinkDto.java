package fr.tduf.gui.database.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

/**
 * Represents a 1-to-many link between 2 topics
 */
@JsonTypeName("topicLink")
public class TopicLinkDto {
    @JsonProperty("topic")
    private DbDto.Topic topic;

    @JsonProperty("group")
    private String group;

    @JsonProperty("priority")
    private int priority;

    @JsonProperty("readOnly")
    private boolean readOnly;

    @JsonProperty("label")
    private String label;

    @JsonProperty("remoteReferenceProfile")
    private String remoteReferenceProfile;

    @JsonProperty("toolTip")
    private String toolTip;

    public DbDto.Topic getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    public String getGroup() {
        return group;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getLabel() {
        return label;
    }

    public int getPriority() {
        return priority;
    }

    public String getRemoteReferenceProfile() {
        return remoteReferenceProfile;
    }

    public String getToolTip() {
        return toolTip;
    }
}