package fr.tduf.libunlimited.high.files.db.patcher.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * Represents a range of items for patch generation (references, field indexes...)
 */
public class ItemRange {

    /** Unique range, accepting all values **/
    public static final ItemRange ALL = ItemRange.global();

    private static final String REGEX_RANGE = "\\d+(,\\d+)*|\\d*\\.\\.\\d*";

    private final RangeBounds bounds;
    private final List<String> enumeratedItems;

    ItemRange(Collection<String> enumeratedItems) {
        this.enumeratedItems = new ArrayList<>(enumeratedItems);
        bounds = null;
    }

    ItemRange(Optional<Long> lowerBound, Optional<Long> upperBound) {
        enumeratedItems = null;
        bounds = RangeBounds.fromBounds(lowerBound, upperBound);
    }

    /**
     * @param potentialRangeOptionValue     : if present, a range under the forms 1,2,3 or 1..3
     * @return a range from a command-line option.
     */
    public static ItemRange fromCliOption(Optional<String> potentialRangeOptionValue) {
        if (!potentialRangeOptionValue.isPresent()) {
            return ALL;
        }

        String rangeOptionValue = potentialRangeOptionValue.get();
        checkValueFormat(rangeOptionValue);

        if (rangeOptionValue.contains("..")) {
            return fromBounds(rangeOptionValue);
        }

        return fromEnumerated(rangeOptionValue);
    }

    /**
     * @param itemValues    : a list of values populating this range
     * @return  a range from a list of values.
     */
    public static ItemRange fromCollection(Collection<String> itemValues) {
        requireNonNull(itemValues, "A collection of item values is required.");

        if (itemValues.isEmpty()) {
            return ALL;
        }

        return new ItemRange(itemValues);
    }

    /**
     * @param itemValue : value to which range will be restricted
     * @return  a range from a single value.
     */
    public static ItemRange fromSingleValue(String itemValue) {
        requireNonNull(itemValue, "An item value is required.");

        return new ItemRange(singletonList(itemValue));
    }

    /**
     * @param itemValue : value to test
     * @return true if provided item value enters current range, false otherwise.
     */
    public boolean accepts(String itemValue) {

        if (isGlobal()) {
            return true;
        }

        if (enumeratedItems != null) {
            return  enumeratedItems.contains(itemValue);
        }

        long reference = Long.parseLong(itemValue);
        return bounds.accept(reference);
    }

    public boolean isGlobal() {
        return bounds != null
                && bounds.isGlobal()
                && enumeratedItems == null;
    }

    private static void checkValueFormat(String rangeOptionValue) {
        Pattern rangePattern = Pattern.compile(REGEX_RANGE);

        if (!rangePattern.matcher(rangeOptionValue).matches()) {
            throw new IllegalArgumentException("Unrecognized range value: " + rangeOptionValue);
        }
    }

    private static ItemRange global() {
        return new ItemRange(Optional.empty(), Optional.empty());
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

    Optional<Long> fetchLowerBound() {
        return Optional.ofNullable(bounds)
                .flatMap(bounds -> bounds.lowerBound);
    }

    Optional<Long> fetchUpperBound() {
        return Optional.ofNullable(bounds)
                .flatMap(bounds -> bounds.upperBound);
    }

    List<String> getEnumeratedItems() {
        return enumeratedItems;
    }

    private static class RangeBounds {
        private final Optional<Long> lowerBound;
        private final Optional<Long> upperBound;

        private RangeBounds(Optional<Long> lowerBound, Optional<Long> upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        private boolean isGlobal() {
            return !lowerBound.isPresent() && !upperBound.isPresent();
        }

        private static RangeBounds fromBounds(Optional<Long> lowerBound, Optional<Long> upperBound) {
            return new RangeBounds(lowerBound, upperBound);
        }

        private boolean accept(long value) {
            return valueAboveUpperBound(value)
                    || valueBelowLowerBound(value)
                    || valueInBounds(value);
        }

        private boolean valueBelowLowerBound(long value) {
            return !upperBound.isPresent()
                    && lowerBound.isPresent()
                    && value >= lowerBound.get();
        }

        private boolean valueAboveUpperBound(long value) {
            return !lowerBound.isPresent()
                    && upperBound.isPresent()
                    && value <= upperBound.get();
        }

        private boolean valueInBounds(long value) {
            return lowerBound.isPresent()
                    && upperBound.isPresent()
                    && value >= lowerBound.get()
                    && value <= upperBound.get();
        }
    }
}
