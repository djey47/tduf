package fr.tduf.libunlimited.low.files.banks.dto;

import java.io.Serializable;

/**
 * Data object to return information about packed file (Bank contents).
 */
public class PackedFileInfoDto implements Serializable {

    private String reference;
    private String fullName;
    private String shortName;
    private String type;
    private int size;

    /**
     * @return a custom instance.
     */
    public static PackedFileInfoDtoBuilder builder() {
        return new PackedFileInfoDtoBuilder(){

            private String shortName;
            private String type;
            private int size;
            private String reference;
            private String fullName;

            @Override
            public PackedFileInfoDto build() {
                PackedFileInfoDto packedFileInfoDto = new PackedFileInfoDto();

                packedFileInfoDto.fullName = this.fullName;
                packedFileInfoDto.reference = this.reference;
                packedFileInfoDto.size = this.size;
                packedFileInfoDto.shortName = this.shortName;
                packedFileInfoDto.type = this.type;

                return packedFileInfoDto;
            }

            @Override
            public PackedFileInfoDtoBuilder forReference(String reference) {
                this.reference = reference;
                return this;
            }

            @Override
            public PackedFileInfoDtoBuilder withSize(int size) {
                this.size = size;
                return this;
            }

            @Override
            public PackedFileInfoDtoBuilder withFullName(String fullName) {
                this.fullName = fullName;
                return this;
            }

            @Override
            public PackedFileInfoDtoBuilder withShortName(String shortName) {
                this.shortName = shortName;
                return this;
            }

            @Override
            public PackedFileInfoDtoBuilder withTypeDescription(String typeDescription) {
                this.type = typeDescription;
                return this;
            }
        };
    }

    public String getReference() {
        return reference;
    }

    public String getFullName() {
        return fullName;
    }

    public int getSize() {
        return size;
    }

    public String getShortName() {
        return shortName;
    }

    public String getType() {
        return type;
    }

    public interface PackedFileInfoDtoBuilder {
        PackedFileInfoDto build();

        PackedFileInfoDtoBuilder forReference(String reference);

        PackedFileInfoDtoBuilder withSize(int size);

        PackedFileInfoDtoBuilder withFullName(String fullName);

        PackedFileInfoDtoBuilder withShortName(String shortName);

        PackedFileInfoDtoBuilder withTypeDescription(String typeDescription);
    }
}