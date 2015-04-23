package fr.tduf.libunlimited.high.files.db.patcher.domain;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a range of entry references for patch generation.
 */
public class ReferenceRange {

    private static final String REGEX_RANGE = "\\d(\\d,)*|\\d*\\.\\.\\d*";

    private final Optional<String> minRef;
    private final Optional<String> maxRef;
    private final List<String> refs;

    /**
     * Creates a range from a command-line option.
     * e.g: 1,2,3 or 1..3
     */
    public static ReferenceRange fromCliOption(Optional<String> rangeOptionValue) {
        if (!rangeOptionValue.isPresent()) {
            return new ReferenceRange(Optional.<String>empty(), Optional.<String>empty());
        }

        checkValueFormat(rangeOptionValue.get());

        return null; //TODO
    }

    /**
     * @return true if provided ref enters current range.
     */
    public boolean accepts(String ref) {
        return isGlobal() || refs.contains(ref);
    }

    ReferenceRange(List<String> refs) {
        this.refs = refs;
        this.minRef = Optional.empty();
        this.maxRef = Optional.empty();
    }

    ReferenceRange(Optional<String> minRef, Optional<String> maxRef) {
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
}