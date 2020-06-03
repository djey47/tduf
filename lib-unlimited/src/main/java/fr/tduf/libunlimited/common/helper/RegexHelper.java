package fr.tduf.libunlimited.common.helper;

/**
 * Utility class for regex handling
 */
public class RegexHelper {
    /**
     * Converts glob (*.*) pattern to regex
     * @return regex pattern
     */
    public static String createRegexFromGlob(String glob) {
        StringBuilder regexPatternBuilder = new StringBuilder();

        for(int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch(c) {
                case '*': regexPatternBuilder.append(".*"); break;
                case '?': regexPatternBuilder.append('.'); break;
                case '.': regexPatternBuilder.append("\\."); break;
                case '\\': regexPatternBuilder.append("\\\\"); break;
                default: regexPatternBuilder.append(c);
            }
        }

        return regexPatternBuilder.toString();
    }
}
