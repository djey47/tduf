package fr.tduf.libunlimited.common.game.domain;

import java.util.stream.Stream;

/**
 * All culture variants for i18n
 */
public enum Locale {
    DEFAULT("*", -1),
    FRANCE("fr", 1),
    GERMANY("ge", 2),
    UNITED_STATES("us", 3),
    KOREA("ko", 4),
    CHINA("ch", 5),
    JAPAN("ja", 6),
    ITALY("it", 7),
    SPAIN("sp", 8);

    private final String code;
    private final int order;

    Locale(String code, int order) {
        this.code = code;
        this.order = order;
    }

    /**
     * Retrieves a locale value from its code.
     */
    public static Locale fromCode(String code) {
        for (Locale locale : values()) {
            if (locale.code.equals(code)) {
                return locale;
            }
        }
        throw new IllegalArgumentException("Unknown Locale code: " + code);
    }

    /**
     * Retrieves a valid locale value from its order.
     */
    public static Locale fromOrder(int order) {
        return valuesAsStream()
                .filter(v -> v.order == order)
                .findAny()
                .orElseThrow(() ->new IllegalArgumentException("Unknown Locale order: " + order));
    }

    public String getCode() {
        return code;
    }

    /**
     * @return all locale values as a stream, except special 'any'
     */
    public static Stream<Locale> valuesAsStream() {
        return Stream.of(values())
                .filter(v -> v != DEFAULT);
    }
}
