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
    // TODO handle readonly indicator if needed

    @JsonProperty("topic")
    private DbDto.Topic topic;

    @JsonProperty("group")
    private String group;

    @JsonProperty("priority")
    private int priority;

    @JsonProperty("label")
    private String label;

    @JsonProperty("remoteReferenceProfile")
    private String remoteReferenceProfile;

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

    public String getLabel() {
        return label;
    }

    public int getPriority() {
        return priority;
    }

    public String getRemoteReferenceProfile() {
        return remoteReferenceProfile;
    }
}