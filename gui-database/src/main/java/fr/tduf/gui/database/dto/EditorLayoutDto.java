package fr.tduf.gui.database.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents all available profiles in Database Editor
 */
// TODO apply code rules
@JsonTypeName("editorLayout")
public class EditorLayoutDto {

    @JsonProperty("profiles")
    List<EditorProfileDto> profiles = new ArrayList<>();

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

        public EditorProfileDto() {}

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

        public void addEntryLabelFieldRank(int fieldRank) {
            entryLabelFieldRanks.add(fieldRank);
        }

        public void setTopic(DbDto.Topic topic) {
            this.topic = topic;
        }
    }
}
