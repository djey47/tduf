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
    // TODO return created entry and use it
    public void addEntry(long hash, long size1, long size2) {
        entries.put(hash, new Entry(hash, size1, size2));
    }

    /**
     * Adds a magic entry. An entry with the same hash should not exist.
     * A magic entry specifies sizes to 0, disabling file size control by the game.
     * @param hash  : unique identifier of bank file to add
     */
    // TODO return created entry and use it
    public void addMagicEntry(long hash) {
        entries.put(hash, new Entry(hash, 0, 0));
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
