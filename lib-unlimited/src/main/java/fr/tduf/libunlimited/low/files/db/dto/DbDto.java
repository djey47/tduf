package fr.tduf.libunlimited.low.files.db.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;

import java.io.Serializable;
import java.util.stream.Stream;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a conytainer for TDU database topic.
 */
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DbDto implements Serializable {
    @JsonProperty("structure")
    private DbStructureDto structure;

    @JsonProperty("data")
    private DbDataDto data;

    @JsonProperty("resource")
    private DbResourceDto resource;

    /**
     * All TDU database topics
     */
    @JsonTypeName("dbTopic")
    public enum Topic {
        ACHIEVEMENTS("TDU_Achievements"),
        AFTER_MARKET_PACKS("TDU_AfterMarketPacks"),
        BOTS("TDU_Bots"),
        BRANDS("TDU_Brands"),
        CAR_COLORS("TDU_CarColors"),
        CAR_PACKS("TDU_CarPacks"),
        CAR_PHYSICS_DATA("TDU_CarPhysicsData"),
        CAR_RIMS("TDU_CarRims"),
        CAR_SHOPS("TDU_CarShops"),
        CLOTHES("TDU_Clothes"),
        HAIR("TDU_Hair"),
        HOUSES("TDU_Houses"),
        INTERIOR("TDU_Interior"),
        MENUS("TDU_Menus"),
        PNJ("TDU_PNJ"),
        RIMS("TDU_Rims"),
        SUB_TITLES("TDU_SubTitles"),
        TUTORIALS("TDU_Tutorials");

        private String label;

        Topic(String label) {
            this.label = label;
        }

        public static Stream<Topic> valuesAsStream() {
            return Stream.of(values());
        }

        /**
         * @return topic, according to provided label.
         */
        public static Topic fromLabel(String label) {

            for (Topic topic : Topic.values()) {
                if (topic.getLabel().equals(label)) {
                    return topic;
                }
            }

            throw new IllegalArgumentException("Unknown topic label: " + label);
        }

        /**
         * @return topic label, according to 'TDU_<topic name, each word capitalized>'.
         */
        public String getLabel() {
            return this.label;
        }
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbDtoBuilder builder() {
        return new DbDtoBuilder();
    }

    public static class DbDtoBuilder {
        private DbDataDto data;
        private DbStructureDto structure;
        private DbResourceDto resource;

        public DbDtoBuilder withStructure(DbStructureDto dbStructureDto) {
            this.structure = dbStructureDto;
            return this;
        }

        public DbDtoBuilder withData(DbDataDto dbDataDto) {
            this.data = dbDataDto;
            return this;
        }

        public DbDtoBuilder withResource(DbResourceDto dbResourceDto) {
            this.resource = dbResourceDto;
            return this;
        }

        public DbDto build() {
            DbDto dbDto = new DbDto();

            dbDto.structure = structure;
            dbDto.data = data;
            dbDto.resource = resource;

            return dbDto;
        }
    }

    /**
     * @return current topic if a structure is attached to current object, null otherwise.
     */
    @JsonIgnore
    public Topic getTopic() {
        if (this.getStructure() == null) {
            return null;
        }
        return this.getStructure().getTopic();
    }

    public DbStructureDto getStructure() {
        return structure;
    }

    public DbDataDto getData() {
        return data;
    }

    public DbResourceDto getResource() {
        return resource;
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o, false);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
