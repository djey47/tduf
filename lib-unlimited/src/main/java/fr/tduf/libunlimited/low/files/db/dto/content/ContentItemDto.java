package fr.tduf.libunlimited.low.files.db.dto.content;

import fr.tduf.libunlimited.high.files.db.common.helper.BitfieldHelper;
import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbStructureDto.FieldType.BITFIELD;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@JsonTypeName("dbEntryItem")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentItemDto {
    @JsonProperty("value")
    private String rawValue;

    @JsonProperty("switches")
    private List<SwitchValueDto> switchValues;

    @JsonProperty("rank")
    private int fieldRank;

    public static ItemBuilder builder() {
        return new ItemBuilder();
    }

    public String getRawValue() {
        return rawValue;
    }

    public int getFieldRank() {
        return fieldRank;
    }

    public List<SwitchValueDto> getSwitchValues() {
        return switchValues;
    }

    @JsonIgnore
    private boolean isBitfield() {
        return switchValues != null;
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

    void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    void shiftFieldRankRight() {
        this.fieldRank++;
    }

    void setFieldRank(int fieldRank) {
        this.fieldRank = fieldRank;
    }

    public static class ItemBuilder {
        private static BitfieldHelper bitfieldHelper = new BitfieldHelper();

        private Integer fieldRank;
        private String raw;

        private boolean isBitField = false;
        private DbDto.Topic topicForBitField;

        public ItemBuilder bitFieldForTopic(boolean isBitField, DbDto.Topic topic) {
            this.isBitField = isBitField;
            this.topicForBitField = topic;
            return this;
        }

        public ItemBuilder fromStructureFieldAndTopic(DbStructureDto.Field field, DbDto.Topic topic) {
            this.fieldRank = field.getRank();

            boolean isBitfield = field.getFieldType() == BITFIELD;
            this.isBitField = isBitfield;
            this.topicForBitField = isBitfield ? topic : null;

            return this;
        }

        public ItemBuilder withRawValue(String rawValue) {
            this.raw = rawValue;
            return this;
        }

        public ItemBuilder ofFieldRank(int fieldRank) {
            this.fieldRank = fieldRank;
            return this;
        }

        public ItemBuilder fromExisting(ContentItemDto contentItem, DbDto.Topic topic) {
            return ContentItemDto.builder()
                    .ofFieldRank(contentItem.getFieldRank())
                    .withRawValue(contentItem.getRawValue())
                    .bitFieldForTopic(contentItem.isBitfield(), topic);
        }

        public ContentItemDto build() {
            requireNonNull(fieldRank, "Rank of associated field must be specified.");

            ContentItemDto item = new ContentItemDto();

            item.rawValue = this.raw;
            item.fieldRank = this.fieldRank;

            if (isBitField) {
                item.switchValues = buildSwitchValues();
            }

            return item;
        }

        private List<SwitchValueDto> buildSwitchValues() {
            requireNonNull(raw, "A raw value is required");
            requireNonNull(topicForBitField, "A database topic is required");

            Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> bitfieldReference = bitfieldHelper.getBitfieldReferenceForTopic(topicForBitField);

            List<SwitchValueDto> switchValues = new ArrayList<>();
            bitfieldReference.ifPresent(refs -> {
                List<Boolean> values = bitfieldHelper.resolve(topicForBitField, raw)
                        .orElseThrow(() -> new IllegalStateException("Bitfield information unavailable for topic: " + topicForBitField));
                refs.forEach(ref -> {
                            boolean switchState = values.get(ref.getIndex() - 1);
                            switchValues.add(new SwitchValueDto(ref.getIndex(), ref.getLabel(), switchState));
                        });
            });

            return switchValues;
        }
    }
}
