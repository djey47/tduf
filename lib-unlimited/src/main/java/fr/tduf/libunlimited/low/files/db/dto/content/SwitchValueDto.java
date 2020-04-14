package fr.tduf.libunlimited.low.files.db.dto.content;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@JsonTypeName("dbBitFieldSwitchValue")
public class SwitchValueDto {
    @JsonProperty("index")
    private final int index;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("enabled")
    private final boolean enabled;

    public SwitchValueDto() {
        index = 0;
        name = null;
        enabled = false;
    }

    public SwitchValueDto(int index, String name, boolean enabled) {
        this.index = index;
        this.enabled = enabled;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
