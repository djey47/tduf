package fr.tduf.libunlimited.high.files.db.dto;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.List;
import java.util.Map;

/**
 * Contains all metadata written in databaseMetadata.json file.
 */
@JsonTypeName("databaseMetadata")
public class DbMetadataDto {

    @JsonProperty("topics")
    private List<TopicMetadataDto> topics;

    @JsonProperty("dealers")
    private List<DealerMetadataDto> dealers;

    @JsonIgnore
    private Map<Integer, String> iks;

    public DbMetadataDto() {}

    public List<TopicMetadataDto> getTopics() {
        return topics;
    }

    public List<DealerMetadataDto> getDealers() {
        return dealers;
    }

    public void setIKs(Map<Integer, String> iks) {
        this.iks = iks;
    }

    public Map<Integer, String> getIKs() {
        return iks;
    }

    @JsonTypeName("topicMetadata")
    public static class TopicMetadataDto {

        public static final int FIELD_RANK_ID_CAR = 102;

        @JsonProperty("topicName")
        private DbDto.Topic topic;

        @JsonProperty("bitfieldReference")
        private List<BitfieldMetadataDto> bitfields;

        @JsonProperty("refSupport")
        private boolean refSupport;

        public TopicMetadataDto() {}

        public DbDto.Topic getTopic() {
            return topic;
        }

        public List<BitfieldMetadataDto> getBitfields() {
            return bitfields;
        }

        public boolean isRefSupport() {
            return refSupport;
        }

        @JsonTypeName("bitfieldMetadata")
        public static class BitfieldMetadataDto {

            @JsonProperty("index")
            private int index;

            @JsonProperty("label")
            private String label;

            @JsonProperty("comment")
            private String comment;

            public BitfieldMetadataDto() {}

            public int getIndex() {
                return index;
            }

            public String getLabel() {
                return label;
            }

            public String getComment() {
                return comment;
            }
        }
    }

    @JsonTypeName("dealerMetadata")
    public static class DealerMetadataDto {

        @JsonProperty("name")
        private String name;

        @JsonProperty("ref")
        private String reference;

        @JsonProperty("availableSlots")
        private List<Integer> availableSlots;

        @JsonProperty("slotCount")
        private int slotCount;

        @JsonProperty("location")
        private String location;

        public DealerMetadataDto() {}

        public List<Integer> getAvailableSlots() {
            return availableSlots;
        }

        public int getSlotCount() {
            return slotCount;
        }

        public String getLocation() {
            return location;
        }

        public String getReference() {
            return reference;
        }

        public String getName() {
            return name;
        }
    }
}
