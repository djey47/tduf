package fr.tduf.libunlimited.low.files.bin.cameras.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.EnumMap;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@JsonTypeName("cameraView")
public class ViewConfigurationDto {
    @JsonProperty("type")
    private ViewKind originalKind;

    @JsonProperty("sourceCameraIdentifier")
    private Integer usedSetIdentifier;

    @JsonProperty("sourceType")
    private ViewKind usedKind;

    @JsonProperty("settings")
    private EnumMap<ViewProps, Object> settings;

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("originalKind", originalKind)
                .toString();
    }

    public ViewKind getOriginalKind() {
        return originalKind;
    }

    public int getUsedSetIdentifier() {
        return usedSetIdentifier;
    }

    public ViewKind getUsedKind() {
        return usedKind;
    }

    public EnumMap<ViewProps, Object> getSettings() {
        return settings;
    }

    public static ViewConfigurationDtoBuilder builder() {
        return new ViewConfigurationDtoBuilder();
    }

    static class ViewConfigurationDtoBuilder {
        private ViewKind kind;
        private EnumMap<ViewProps, Object> settings;
        private Integer usedSetIdentifier;
        private ViewKind usedViewKind;

        ViewConfigurationDtoBuilder forKind(ViewKind kind) {
            this.kind = kind;
            return this;
        }

        ViewConfigurationDtoBuilder withSettings(EnumMap<ViewProps, Object> settings) {
            this.settings = settings;
            return this;
        }

        ViewConfigurationDtoBuilder withUsedSetIdentifier(Integer setIdentifier) {
            this.usedSetIdentifier = setIdentifier;
            return this;
        }

        ViewConfigurationDtoBuilder witUsedViewKind(ViewKind usedKind) {
            this.usedViewKind = usedKind;
            return this;
        }

        ViewConfigurationDto build() {
            ViewConfigurationDto viewConfigurationDto = new ViewConfigurationDto();

            viewConfigurationDto.originalKind = kind;
            viewConfigurationDto.settings = settings;
            viewConfigurationDto.usedKind = usedViewKind ;
            viewConfigurationDto.usedSetIdentifier = usedSetIdentifier ;

            return viewConfigurationDto;
        }
    }
}
