package fr.tduf.libunlimited.common.game.domain;

import java.util.stream.Stream;

/**
 * All culture variants for i18n
 */
public enum Locale {
    ANY("*"),
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

    /**
     * @return all locale values as a stream, except special 'any'
     */
    public static Stream<Locale> valuesAsStream() {
        return Stream.of(values())
                .filter(v -> v != ANY);
    }
}
