package fr.tduf.libunlimited.low.files.db.dto;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * Represents a TDU database topic.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbDto implements Serializable {

    /**
     * @return builder, used to generate custom values.
     */
    public static DbDtoBuilder builder() {
        return new DbDtoBuilder() {
            @Override
            public DbDtoBuilder forName(String name) {
                return this;
            }

            @Override
            public DbDtoBuilder forRef(String ref) {
                return this;
            }

            @Override
            public DbDtoBuilder withStructure(DbStructureDto dbStructureDto) {
                return this;
            }

            @Override
            public DbDtoBuilder withData(DbDataDto dbDataDto) {
                return this;
            }

            @Override
            public DbDtoBuilder withResources(DbResourceDto dbResourcesDto) {
                return this;
            }

            @Override
            public DbDto build() {
                DbDto dbDto = new DbDto();



                return dbDto;
            }
        };
    }

    public interface DbDtoBuilder {
        DbDtoBuilder forName(String name);

        DbDtoBuilder forRef(String ref);

        DbDtoBuilder withStructure(DbStructureDto dbStructureDto);

        DbDtoBuilder withData(DbDataDto dbDataDto);

        DbDtoBuilder withResources(DbResourceDto dbResourcesDto);

        DbDto build();
    }
}
