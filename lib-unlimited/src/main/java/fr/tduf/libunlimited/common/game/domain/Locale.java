package fr.tduf.libunlimited.common.game.domain;

import java.util.stream.Stream;

/**
 * All culture variants for i18n
 */
// TODO introduce any locale but do not return it in stream
public enum Locale {
    FRANCE("fr"),
    GERMANY("ge"),
    UNITED_STATES("us"),
    KOREA("ko"),
    CHINA("ch"),
    JAPAN("ja"),
    ITALY("it"),
    SPAIN("sp");

    private final String code;

    Locale(String code) {
        this.code = code;
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

    public String getCode() {
        return code;
    }

    public static Stream<Locale> valuesAsStream() {
        // TODO do not include 'any'
        return Stream.of(values());
    }
}
