package fr.tduf.libunlimited.low.files.banks.dto;

/**
 * Data object to return information about packed file (Bank contents).
 */
public class PackedFileInfoDto {

    private String reference;
    private String fullName;
    private int size;

    /**
     * @return a custom instance.
     */
    public static PackedFileInfoDtoBuilder builder() {
        return new PackedFileInfoDtoBuilder(){

            private int size;
            private String reference;
            private String fullName;

            @Override
            public PackedFileInfoDto build() {
                PackedFileInfoDto packedFileInfoDto = new PackedFileInfoDto();

                packedFileInfoDto.fullName = this.fullName;
                packedFileInfoDto.reference = this.reference;
                packedFileInfoDto.size = this.size;

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

    public interface PackedFileInfoDtoBuilder {
        PackedFileInfoDto build();

        PackedFileInfoDtoBuilder forReference(String reference);

        PackedFileInfoDtoBuilder withSize(int size);

        PackedFileInfoDtoBuilder withFullName(String fullName);
    }
}