package fr.tduf.gui.database.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

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

    @JsonProperty("label")
    private String label;

    @JsonProperty("remoteFieldRanks")
    private List<Integer> remoteFieldRanks = new ArrayList<>();

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

    public List<Integer> getRemoteFieldRanks() {
        return remoteFieldRanks;
    }
}