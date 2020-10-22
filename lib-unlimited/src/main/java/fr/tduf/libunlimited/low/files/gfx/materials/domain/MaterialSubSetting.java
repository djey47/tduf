package fr.tduf.libunlimited.low.files.gfx.materials.domain;

/**
 * Represents a sub setting for a material
 */
public class MaterialSubSetting {
    long id;
    long value1;
    long value2;
    long value3;

    public MaterialSubSetting(long id, long value1, long value2, long value3) {
        this.id = id;
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    public long getId() {
        return id;
    }

    public long getValue1() {
        return value1;
    }

    public long getValue2() {
        return value2;
    }

    public long getValue3() {
        return value3;
    }
}
