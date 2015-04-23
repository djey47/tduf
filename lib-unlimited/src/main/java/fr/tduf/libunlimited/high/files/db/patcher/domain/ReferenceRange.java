package fr.tduf.libunlimited.high.files.db.patcher.domain;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

/**
 * Represents a range of entry references for patch generation.
 */
public class ReferenceRange {

    private static final String REGEX_RANGE = "\\d(,\\d)*|\\d*\\.\\.\\d*";

    private final Optional<Long> minRef;
    private final Optional<Long> maxRef;
    private final List<String> refs;

    /**
     * Creates a range from a command-line option.
     * e.g: 1,2,3 or 1..3
     */
    public static ReferenceRange fromCliOption(Optional<String> potentialRangeOptionValue) {
        if (!potentialRangeOptionValue.isPresent()) {
            return new ReferenceRange(Optional.<Long>empty(), Optional.<Long>empty());
        }

        String rangeOptionValue = potentialRangeOptionValue.get();
        checkValueFormat(rangeOptionValue);

        if (rangeOptionValue.contains("..")) {
            return fromBounds(rangeOptionValue);
        }

        return fromEnumerated(rangeOptionValue);
    }

    /**
     * @return true if provided ref enters current range, false otherwise.
     */
    public boolean accepts(String ref) {

        if (isGlobal()) {
            return true;
        }

        if (refs != null) {
            return  refs.contains(ref);
        }

        long reference = Long.parseLong(ref);
        return (!minRef.isPresent() && reference <= maxRef.get())
                || (!maxRef.isPresent() && reference >= minRef.get())
                || (reference >= minRef.get() && reference <= maxRef.get());
    }

    ReferenceRange(List<String> refs) {
        this.refs = refs;
        this.minRef = Optional.empty();
        this.maxRef = Optional.empty();
    }

    ReferenceRange(Optional<Long> minRef, Optional<Long> maxRef) {
        this.refs = null;
        this.minRef = minRef;
        this.maxRef = maxRef;
    }

    boolean isGlobal() {
        return !this.minRef.isPresent()
                && !this.maxRef.isPresent()
                && this.refs == null;
    }

    private static void checkValueFormat(String rangeOptionValue) {
        Pattern rangePattern = Pattern.compile(REGEX_RANGE);

        if (!rangePattern.matcher(rangeOptionValue).matches()) {
            throw new IllegalArgumentException("Unrecognized range value: " + rangeOptionValue);
        }
    }

    private static ReferenceRange fromEnumerated(String enumeratedRange) {
        String[] items = enumeratedRange.split(",");
        return new ReferenceRange(asList(items));
    }

    private static ReferenceRange fromBounds(String boundedRange) {
        String[] bounds = boundedRange.split("\\.\\.");
        long lowerBound = Integer.valueOf(bounds[0]);
        long upperBound = Integer.valueOf(bounds[1]);

        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Invalid range: " + lowerBound + ".." + upperBound);
        }

        return new ReferenceRange(Optional.of(lowerBound), Optional.of(upperBound));
    }

    Optional<Long> getMinRef() {
        return minRef;
    }

    Optional<Long> getMaxRef() {
        return maxRef;
    }

    List<String> getRefs() {
        return refs;
    }
}