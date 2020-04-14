package fr.tduf.gui.database.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import java.util.ArrayList;
import java.util.List;

import static fr.tduf.gui.database.common.DisplayConstants.DATA_FORMAT_LAYOUT_OBJECT;

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
            return String.format(DATA_FORMAT_LAYOUT_OBJECT, this.getTopic(), this.getName());
        }
    }
}
