package fr.tduf.gui.database.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents all available profiles in Database Editor
 */
@JsonTypeName("editorLayout")
public class EditorLayoutDto {

    @JsonProperty("profiles")
    private List<EditorProfileDto> profiles = new ArrayList<>();

    public List<EditorProfileDto> getProfiles() {
        return profiles;
    }

    @JsonTypeName("editorProfile")
    public static class EditorProfileDto {

        @JsonProperty("name")
        private String name;

        @JsonProperty("topic")
        private DbDto.Topic topic;

        @JsonProperty("entryLabelFieldRanks")
        private List<Integer> entryLabelFieldRanks = new ArrayList<>();

        @JsonProperty("fieldSettings")
        private List<FieldSettingsDto> fieldSettings = new ArrayList<>();

        @JsonProperty("groups")
        private List<String> groups = new ArrayList<>();

        @JsonProperty("topicLinks")
        private List<TopicLinkDto> topicLinks = new ArrayList<>();

        public EditorProfileDto() {
            // Required by jackson
        }

        public EditorProfileDto(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public DbDto.Topic getTopic() {
            return topic;
        }

        public List<FieldSettingsDto> getFieldSettings() {
            return fieldSettings;
        }

        public List<String> getGroups() {
            return groups;
        }

        public List<TopicLinkDto> getTopicLinks() {
            return topicLinks;
        }

        public List<Integer> getEntryLabelFieldRanks() {
            return entryLabelFieldRanks;
        }

        public void addDefaultEntryLabelFieldRank() {
            entryLabelFieldRanks.add(1);
        }

        public void setTopic(DbDto.Topic topic) {
            this.topic = topic;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s", this.getTopic(), this.getName());
        }
    }
}
