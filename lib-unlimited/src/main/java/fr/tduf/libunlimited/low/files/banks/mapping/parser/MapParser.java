package fr.tduf.libunlimited.low.files.banks.mapping.parser;

import java.io.ByteArrayInputStream;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to fetch entry list contained in Bnk1.map file.
 */
public class MapParser {

    private final ByteArrayInputStream mapInputStream;

    private MapParser(ByteArrayInputStream inputStream) {
        this.mapInputStream = inputStream;
    }

    /**
     * Single entry point for this parser.
     * @return a {@link MapParser} instance.
     */
    public static MapParser load(ByteArrayInputStream inputStream) {
        requireNonNull(inputStream, "A stream containing map contents is required");

        return new MapParser(inputStream);
    }

    ByteArrayInputStream getMapInputStream() {
        return mapInputStream;
    }
}
