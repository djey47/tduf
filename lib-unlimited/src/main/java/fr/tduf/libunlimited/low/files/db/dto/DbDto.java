package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * Represents a TDU database topic.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbDto implements Serializable {

    @JsonProperty("structure")
    private DbStructureDto structure;

    @JsonProperty("data")
    private DbDataDto data;

    @JsonProperty("resources")
    private DbResourceDto resources;

    @JsonProperty("ref")
    public String getRef() {
        return structure.getRef();
    }

    @JsonProperty("name")
    public String getName() {
        return structure.getName();
    }

    /**
     * @return builder, used to generate custom values.
     */
    public static DbDtoBuilder builder() {
        return new DbDtoBuilder() {
            private DbResourceDto resources;
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
            public DbDtoBuilder withResources(DbResourceDto dbResourcesDto) {
                this.resources = dbResourcesDto;
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

    public DbResourceDto getResources() {
        return resources;
    }

    public interface DbDtoBuilder {
        DbDtoBuilder withStructure(DbStructureDto dbStructureDto);

        DbDtoBuilder withData(DbDataDto dbDataDto);

        DbDtoBuilder withResources(DbResourceDto dbResourcesDto);

        DbDto build();
    }
}