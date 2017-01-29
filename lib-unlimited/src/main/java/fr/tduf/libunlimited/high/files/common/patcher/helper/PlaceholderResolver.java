package fr.tduf.libunlimited.high.files.common.patcher.helper;

import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parent class for all resolvers
 */
public abstract class PlaceholderResolver {
    public static final String FORMAT_PLACEHOLDER = "{%s}";
    protected static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("\\{(.+)}");                         // e.g {FOO}

    protected PatchProperties patchProperties = new PatchProperties();

    /**
     * Main component entry point
     */
    public abstract void resolveAllPlaceholders();

    protected String resolveSimplePlaceholder(String value) {
        final Matcher matcher = PATTERN_PLACEHOLDER.matcher(value);
        if (matcher.matches()) {
            final String placeholderName = matcher.group(1);
            return patchProperties.retrieve(placeholderName)
                    .orElseThrow(() -> new IllegalArgumentException("No property provided for placeholder: " + placeholderName));
        }

        return value;
    }
}
