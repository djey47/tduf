package fr.tduf.gui.database.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.List;

/**
 * Represents all availables profiles in Database Editor
 */
@JsonTypeName("editorLayout")
public class EditorLayoutDto {

    @JsonProperty("profiles")
    List<EditorProfileDto> profiles;

    public List<EditorProfileDto> getProfiles() {
        return profiles;
    }

    @JsonTypeName("editorProfile")
    public static class EditorProfileDto {

        @JsonProperty("name")
        private String name;

        @JsonProperty("topic")
        private DbDto.Topic topic;

        @JsonProperty("fieldSettings")
        private List<FieldSettingsDto> fieldSettings;

        public String getName() {
            return name;
        }
    }
}