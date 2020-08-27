package fr.tduf.libunlimited.common.game.helper;

/**
 * Provides methods helping with some game engine specificities
 */
public class GameEngineHelper {
    private GameEngineHelper() {}

    /**
     * Converts strings exceeding 8 characters to 8 characters with ASCII code hack. Used for material names, cameras.
     * Overlapping strings have their ASCII code added to the begin of the String (modulo 8)
     * e.g FORDGT_02 => xORDGT_0
     * F <=> 70, 2 <=> 50, so the first character will be replaced by x <=> (70 + 50)
     *
     * @param toBeNormalized - String to be normalized, if necessary
     * @return normalized string
     */
    public static String normalizeString(String toBeNormalized) {
        final int MAX_SIZE = 8;

        if (toBeNormalized == null) {
            return null;
        }

        if (toBeNormalized.length() <= MAX_SIZE) {
            return toBeNormalized;
        }

        char[] basePart = toBeNormalized.substring(0, MAX_SIZE).toCharArray();
        char[] overlappingPart = toBeNormalized.substring(MAX_SIZE).toCharArray();

        for (int i = 0 ; i < overlappingPart.length ; i++) {
            basePart[i % MAX_SIZE] += overlappingPart[i];
        }

        return new String(basePart);
    }
}
