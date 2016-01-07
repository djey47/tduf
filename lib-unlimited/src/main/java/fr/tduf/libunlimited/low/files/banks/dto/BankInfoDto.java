package fr.tduf.libunlimited.low.files.banks.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data object to return information about Bnk file.
 */
public class BankInfoDto implements Serializable {

    private int year;
    private int fileSize;
    private final List<PackedFileInfoDto> packedFiles = new ArrayList<>();

    private BankInfoDto(){}

    /**
     * @return custom instance.
     */
    public static BankInfoDtoBuilder builder() {
        return new BankInfoDtoBuilder(){

            private final List<PackedFileInfoDto> packedFiles = new ArrayList<>();
            private int year;
            private int fileSize;

            @Override
            public BankInfoDtoBuilder fromYear(int year) {
                this.year = year;
                return this;
            }

            @Override
            public BankInfoDtoBuilder withFileSize(int fileSize) {
                this.fileSize = fileSize;
                return this;
            }

            @Override
            public BankInfoDtoBuilder addPackedFile(PackedFileInfoDto packedFileInfoDto) {
                this.packedFiles.add(packedFileInfoDto);
                return this;
            }

            @Override
            public BankInfoDtoBuilder addPackedFiles(List<PackedFileInfoDto> packedFilesInfos) {
                this.packedFiles.addAll(packedFilesInfos);
                return this;
            }

            @Override
            public BankInfoDto build() {
                BankInfoDto bankInfoDto = new BankInfoDto();

                bankInfoDto.fileSize = this.fileSize;
                bankInfoDto.year = this.year;
                bankInfoDto.packedFiles.addAll(this.packedFiles);

                return bankInfoDto;
            }
        };
    }

    public int getYear() {
        return year;
    }

    public int getFileSize() {
        return fileSize;
    }

    public List<PackedFileInfoDto> getPackedFiles() {
        return packedFiles;
    }

    public interface BankInfoDtoBuilder {
        BankInfoDtoBuilder fromYear(int year);

        BankInfoDtoBuilder withFileSize(int fileSize);

        BankInfoDtoBuilder addPackedFile(PackedFileInfoDto packedFileInfoDto);

        BankInfoDtoBuilder addPackedFiles(List<PackedFileInfoDto> packedFilesInfos);

        BankInfoDto build();
    }
}