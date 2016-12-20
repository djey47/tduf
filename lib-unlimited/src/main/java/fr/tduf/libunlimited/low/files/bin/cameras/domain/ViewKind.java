package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import java.util.stream.Stream;

/**
 * All handled view kinds
 **/
public enum ViewKind {
    Follow_Near(20), Follow_Near_Back(40),
    Follow_Far(21), Follow_Far_Back(41),
    Bumper(22), Bumper_Back(42),
    Cockpit(23), Cockpit_Back(43),
    Hood(24), Hood_Back(44),
    Follow_Large(25), Follow_Large_Back(45),
    Unknown(0);

    private int internalId;

    ViewKind(int internalId) {
        this.internalId = internalId;
    }

    public static ViewKind fromInternalId(int internalId) {
        return Stream.of(values())
                .filter(type -> type.internalId == internalId)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown view type identifier: " + internalId));
    }

    public int getInternalId() {
        return internalId;
    }
}
