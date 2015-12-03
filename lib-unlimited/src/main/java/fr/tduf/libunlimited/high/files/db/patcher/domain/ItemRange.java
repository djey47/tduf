package fr.tduf.libunlimited.high.files.db.patcher.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Represents a range of items for patch generation (references, field indexes...)
 */
public class ItemRange {

    private static final String REGEX_RANGE = "\\d+(,\\d+)*|\\d*\\.\\.\\d*";

    private final Optional<Long> lowerBound;
    private final Optional<Long> upperBound;
    private final List<String> enumeratedItems;

    /**
     * Creates a range from a command-line option.
     * e.g: 1,2,3 or 1..3
     */
    public static ItemRange fromCliOption(Optional<String> potentialRangeOptionValue) {
        if (!potentialRangeOptionValue.isPresent()) {
            return new ItemRange();
        }

        String rangeOptionValue = potentialRangeOptionValue.get();
        checkValueFormat(rangeOptionValue);

        if (rangeOptionValue.contains("..")) {
            return fromBounds(rangeOptionValue);
        }

        return fromEnumerated(rangeOptionValue);
    }

    /**
     * Creates a range from a list of values.
     */
    public static ItemRange fromCollection(Collection<String> itemValues) {
        requireNonNull(itemValues, "A collection of item values is required.");

        if (itemValues.isEmpty()) {
            return new ItemRange();
        }

        return new ItemRange(itemValues);
    }

    /**
     * @return true if provided item values enters current range, false otherwise.
     */
    public boolean accepts(String itemValue) {

        if (isGlobal()) {
            return true;
        }

        if (enumeratedItems != null) {
            return  enumeratedItems.contains(itemValue);
        }

        long reference = Long.parseLong(itemValue);
        return (!lowerBound.isPresent() && reference <= upperBound.get())
                || (!upperBound.isPresent() && reference >= lowerBound.get())
                || (reference >= lowerBound.get() && reference <= upperBound.get());
    }

    ItemRange() {
        this(Optional.<Long>empty(), Optional.<Long>empty());
    }

    ItemRange(Collection<String> enumeratedItems) {
        this.enumeratedItems = new ArrayList<>(enumeratedItems);
        this.lowerBound = Optional.empty();
        this.upperBound = Optional.empty();
    }

    ItemRange(Optional<Long> lowerBound, Optional<Long> upperBound) {
        this.enumeratedItems = null;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    boolean isGlobal() {
        return !this.lowerBound.isPresent()
                && !this.upperBound.isPresent()
                && this.enumeratedItems == null;
    }

    private static void checkValueFormat(String rangeOptionValue) {
        Pattern rangePattern = Pattern.compile(REGEX_RANGE);

        if (!rangePattern.matcher(rangeOptionValue).matches()) {
            throw new IllegalArgumentException("Unrecognized range value: " + rangeOptionValue);
        }
    }

    private static ItemRange fromEnumerated(String enumeratedRange) {
        String[] items = enumeratedRange.split(",");
        return new ItemRange(asList(items));
    }

    private static ItemRange fromBounds(String boundedRange) {
        String[] bounds = boundedRange.split("\\.\\.");
        long lowerBound = Integer.valueOf(bounds[0]);
        long upperBound = Integer.valueOf(bounds[1]);

        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Invalid range: " + lowerBound + ".." + upperBound);
        }

        return new ItemRange(Optional.of(lowerBound), Optional.of(upperBound));
    }

    Optional<Long> getLowerBound() {
        return lowerBound;
    }

    Optional<Long> getUpperBound() {
        return upperBound;
    }

    List<String> getEnumeratedItems() {
        return enumeratedItems;
    }
}
