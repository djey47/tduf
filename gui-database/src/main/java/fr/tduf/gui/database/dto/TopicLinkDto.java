package fr.tduf.gui.database.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

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

    public DbDto.Topic getTopic() {
        return topic;
    }

    public String getGroup() {
        return group;
    }

    public String getLabel() {
        return label;
    }
}