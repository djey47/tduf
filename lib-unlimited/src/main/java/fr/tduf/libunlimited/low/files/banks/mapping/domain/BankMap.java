package fr.tduf.libunlimited.low.files.banks.mapping.domain;

import java.util.*;

/**
 * Represents contents of Bnk1.map file.
 */
public class BankMap {

    private final Map<Long, Entry> entries = new HashMap<>();

    /**
     * Adds a new entry. An entry with the same hash should not exist.
     * @param hash  : unique identifier of bank file to add
     * @param size1 : reference size 1
     * @param size2 : reference size 2
     */
    public void addEntry(long hash, long size1, long size2) {
        entries.put(hash, new Entry(hash, size1, size2));
    }

    /**
     * Adds all specified entries. Entries with same hashes should not exist.
     */
    public void addAllEntries(Map<Long, Entry> entries) {
        entries.putAll(entries);
    }

    /**
     * Adds a magic entry. An entry with the same hash should not exist.
     * A magic entry specifies sizes to 0, disabling file size control by the game.
     * @param hash  : unique identifier of bank file to add
     */
    public void addMagicEntry(long hash) {
        entries.put(hash, new Entry(hash, 0, 0));
    }

    public Collection<Entry> getEntries() {
        return entries.values();
    }

    public Set<Long> getChecksums() {
        return entries.keySet();
    }

    /**
     * Structure representing a bank entry
     */
    public class Entry {
        private final  long hash;
        private final long size1;
        private final long size2;

        private Entry(long hash, long size1, long size2) {
            this.hash = hash;
            this.size1 = size1;
            this.size2 = size2;
        }

        @Override
        public String toString() {
            return '{' +
                    "hash=" + hash +
                    ", size1=" + size1 +
                    ", size2=" + size2 +
                    '}';
        }
    }
}