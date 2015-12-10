package fr.tduf.libunlimited.high.files.db.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * An entry field value for selection and changes.
 */
@JsonTypeName("dbFieldChangeValue")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DbFieldValueDto {

    @JsonProperty("rank")
    private final int rank;

    @JsonProperty("value")
    private final String value;

    /** For Jackson **/
    private DbFieldValueDto() {
        this(0, null);
    }

    private DbFieldValueDto(int rank, String value) {
        this.rank = rank;
        this.value = value;
    }

    public static DbFieldValueDto fromCouple(int rank, String value) {
        return new DbFieldValueDto(rank, value);
    }

    public int getRank() {
        return rank;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() { return reflectionToString(this); }

    @Override
    public boolean equals(Object o) { return reflectionEquals(this, o); }

    @Override
    public int hashCode() { return reflectionHashCode(this); }
}
