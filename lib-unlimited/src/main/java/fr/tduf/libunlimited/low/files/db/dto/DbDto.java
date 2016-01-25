package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a conytainer for TDU database topic.
 */
// TODO  Add method to get by locale
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbDto implements Serializable {
    @JsonProperty("structure")
    private DbStructureDto structure;

    @JsonProperty("data")
    private DbDataDto data;

    @JsonProperty("resources")
    private List<DbResourceDto> resources;

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

        /**
         * @return topic, according to provided label.
         */
        public static Topic fromLabel(String label) {

            for(Topic topic : Topic.values()) {
                if(topic.getLabel().equals(label)) {
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
        return new DbDtoBuilder() {
            private final List<DbResourceDto> resources = new ArrayList<>();
            private DbDataDto data;
            private DbStructureDto structure;

            @Override
            public DbDtoBuilder withStructure(DbStructureDto dbStructureDto) {
                this.structure = dbStructureDto;
                return this;
            }

            @Override
            public DbDtoBuilder withData(DbDataDto dbDataDto) {
                this.data = dbDataDto;
                return this;
            }

            @Override
            public DbDtoBuilder addResources(List<DbResourceDto> dbResourcesDtos) {
                this.resources.addAll(dbResourcesDtos);
                return this;
            }

            @Override
            public DbDtoBuilder addResource(DbResourceDto dbResourceDto) {
                this.resources.add(dbResourceDto);
                return this;
            }

            @Override
            public DbDto build() {
                DbDto dbDto = new DbDto();

                dbDto.structure = this.structure;
                dbDto.data = this.data;
                dbDto.resources = this.resources;

                return dbDto;
            }
        };
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

    public List<DbResourceDto> getResources() {
        return resources;
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

    public interface DbDtoBuilder {
        DbDtoBuilder withStructure(DbStructureDto dbStructureDto);

        DbDtoBuilder withData(DbDataDto dbDataDto);

        DbDtoBuilder addResources(List<DbResourceDto> dbResourcesDtos);

        DbDtoBuilder addResource(DbResourceDto dbResourceDto);

        DbDto build();
    }
}