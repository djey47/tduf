package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a conytainer for TDU database topic.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbDto implements Serializable {

    @JsonProperty("structure")
    private DbStructureDto structure;

    @JsonProperty("data")
    private DbDataDto data;

    @JsonProperty("resources")
    private List<DbResourceDto> resources;

    @JsonProperty("ref")
    public String getRef() {
        return structure.getRef();
    }

    @JsonProperty("topic")
    public Topic getTopic() {
        return structure.getTopic();
    }

    /**
     * All TDU database topics
     */
    @JsonTypeName("dbTopic")
    public enum Topic {
        ACHIEVEMENTS,
        AFTER_MARKET_PACKS,
        BOTS,
        BRANDS,
        CAR_COLORS,
        CAR_PACKS,
        CAR_PHYSICS_DATA,
        CAR_RIMS,
        CAR_SHOPS,
        CLOTHES,
        HAIR,
        HOUSES,
        INTERIOR,
        MENUS,
        PNJ,
        RIMS,
        SUB_TITLES,
        TUTORIALS;

        /**
         * @return topic label, according to 'TDU_<topic name, each word capitalized>'
         */
        public static String getLabel(Topic topic) {

            StringBuilder labelBuilder = new StringBuilder();
            labelBuilder.append("TDU_");

            for (String component : topic.name().split("_")) {
                labelBuilder.append(component.substring(0, 1).toUpperCase());
                labelBuilder.append(component.substring(1).toLowerCase());
            }

            return labelBuilder.toString();
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

    public DbStructureDto getStructure() {
        return structure;
    }

    public DbDataDto getData() {
        return data;
    }

    public List<DbResourceDto> getResources() {
        return resources;
    }

    public interface DbDtoBuilder {
        DbDtoBuilder withStructure(DbStructureDto dbStructureDto);

        DbDtoBuilder withData(DbDataDto dbDataDto);

        DbDtoBuilder addResources(List<DbResourceDto> dbResourcesDtos);

        DbDtoBuilder addResource(DbResourceDto dbResourceDto);

        DbDto build();
    }
}