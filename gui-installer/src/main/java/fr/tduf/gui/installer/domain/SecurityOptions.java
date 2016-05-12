package fr.tduf.gui.installer.domain;

import java.math.BigDecimal;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Class describing security options 1&2 for a vehicle slot
 */
public class SecurityOptions {
    public static final float ONE_DEFAULT = 1;
    public static final int TWO_DEFAULT = 100;

    public static final BigDecimal NOT_INSTALLED = BigDecimal.valueOf(1.0);
    public static final BigDecimal INSTALLED = BigDecimal.valueOf(100.0);

    private final float optionOne;
    private int optionTwo;

    private SecurityOptions(float one, int two) {
        optionOne = one;
        optionTwo = two;
    }

    public static SecurityOptions fromValues(float one, int two) {
        return new SecurityOptions(one, two);
    }

    public BigDecimal getOptionOne() {
        return BigDecimal.valueOf(optionOne);
    }

    public int getOptionTwo() {
        return optionTwo;
    }

    @Override
    public boolean equals(Object o) { return reflectionEquals(this, o); }

    @Override
    public int hashCode() { return reflectionHashCode(this); }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
