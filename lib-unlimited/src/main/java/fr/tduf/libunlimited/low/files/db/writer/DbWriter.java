package fr.tduf.libunlimited.low.files.db.writer;

import fr.tduf.libunlimited.low.files.db.dto.DbDto;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to generate db files (contents+structure) from database instances.
 */
public class DbWriter {

    private final DbDto databaseDto;

    private DbWriter(DbDto dbDto) {
        this.databaseDto = dbDto;
    }

    /**
     * Single entry point for this writer.
     * @param dbDto full database information
     * @return writer instance.
     */
    public static DbWriter load(DbDto dbDto) {
        requireNonNull(dbDto, "Full database information is required");

        return new DbWriter(dbDto);
    }

    public void writeAll(String path) {

    }
}
