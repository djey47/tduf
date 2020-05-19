package fr.tduf.libunlimited.common.game.domain;

import java.util.stream.Stream;

/**
 * All culture variants for i18n
 */
public enum Locale {
    DEFAULT("*", "any", -1),
    FRANCE("fr", "Français", 1),
    GERMANY("ge", "Deutsch", 2),
    UNITED_STATES("us", "English (united states)", 3),
    KOREA("ko", "한국어", 4),
    CHINA("ch", "中文", 5),
    JAPAN("ja", "日本語", 6),
    ITALY("it", "Italiano", 7),
    SPAIN("sp", "Español", 8);

    private final String code;
    private final String language;
    private final int order;

    Locale(String code, String language, int order) {
        this.code = code;
        this.language = language;
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

    public String getLanguage() {
        return language;
    }
    
    /**
     * @return all locale values as a stream, except special 'any'
     */
    public static Stream<Locale> valuesAsStream() {
        return Stream.of(values())
                .filter(v -> v != DEFAULT);
    }
}
