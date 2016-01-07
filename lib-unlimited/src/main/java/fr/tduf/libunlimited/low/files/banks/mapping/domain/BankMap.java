package fr.tduf.libunlimited.low.files.banks.mapping.domain;

import java.util.*;

/**
 * Represents contents of Bnk1.map file.
 */
public class BankMap {

    private final Map<Long, Entry> entries = new HashMap<>();

    private byte[] entrySeparator;
    private String tag;

    /**
     * Adds a new entry. An entry with the same hash should not exist.
     * @param hash  : unique identifier of bank file to add
     * @param size1 : reference size 1
     * @param size2 : reference size 2
     */
    public Entry addEntry(long hash, long size1, long size2) {
        Entry newEntry = new Entry(hash, size1, size2);

        entries.put(hash, newEntry);

        return newEntry;
    }

    /**
     * Adds a magic entry. An entry with the same hash should not exist.
     * A magic entry specifies sizes to 0, disabling file size control by the game.
     * @param hash  : unique identifier of bank file to add
     */
    public Entry addMagicEntry(long hash) {
        Entry newMagicEntry = new Entry(hash, 0, 0);

        entries.put(hash, newMagicEntry);

        return newMagicEntry;
    }

    /**
     * Makes all entries magic.
     */
    public void magifyAll() {

        getEntries().stream().parallel()

                .filter( (entry) -> !entry.isMagic())

                .forEach(Entry::magify);
    }

    /**
     * Returns all entries in this map.
     * @return an unsorted collection of {@link Entry}
     */
    public Collection<Entry> getEntries() {
        return entries.values();
    }

    /**
     * Returns all keys (checksums) in this map.
     * @return an unsorted collection of checksums as {@link java.lang.Long}
     */
    public Set<Long> getChecksums() {
        return entries.keySet();
    }

    /**
     * @return true if current map is a magic one.
     */
    public boolean isMagic() {

        if (entries.isEmpty()) {
            return false;
        }

        return getEntries().stream().parallel()

                .filter( (entry) -> !entry.isMagic() )

                .findAny()

                .map( (entry) -> false )

                .orElse(true);
    }

    public byte[] getEntrySeparator() {
        return entrySeparator;
    }

    public void setEntrySeparator(byte[] entrySeparator) {
        this.entrySeparator = entrySeparator;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    /**
     * Structure representing a bank entry
     */
    public class Entry {
        private final long hash;
        private long size1;
        private long size2;

        private Entry(long hash, long size1, long size2) {
            this.hash = hash;
            this.size1 = size1;
            this.size2 = size2;
        }

        /**
         * Disables size control onto this entry.
         * Will set sizes to 0.
         */
        public void magify() {
            size1 = size2 = 0;
        }

        /**
         * @return true if current entry is magic
         */
        public boolean isMagic() {
            return size1 == 0 && size2 == 0;
        }

        @Override
        public String toString() {
            return '{' +
                    "hash=" + hash +
                    ", size1=" + size1 +
                    ", size2=" + size2 +
                    '}';
        }

        public long getHash() {
            return hash;
        }

        public long getSize1() {
            return size1;
        }

        public long getSize2() {
            return size2;
        }
    }
}
