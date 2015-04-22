package fr.tduf.libunlimited.high.files.db.patcher.domain;

import java.util.List;
import java.util.Optional;

/**
 * Represents a range of entry references for patch generation.
 */
public class ReferenceRange {

    private final Optional<String> minRef;
    private final Optional<String> maxRef;
    private final List<String> refs;

    /**
     *
     * @param rangeOptionValue
     * @return
     */
    public static ReferenceRange fromCliOption(Optional<String> rangeOptionValue) {
        if (!rangeOptionValue.isPresent()) {
            return new ReferenceRange(Optional.<String>empty(), Optional.<String>empty());
        }

        return null; //TODO
    }

    /**
     *
     * @param ref
     * @return
     */
    public boolean accepts(String ref) {
        if (isGlobal()) {
            return true;
        }

        if (refs.contains(ref)) {
            return true;
        }

        return false; //TODO
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
}