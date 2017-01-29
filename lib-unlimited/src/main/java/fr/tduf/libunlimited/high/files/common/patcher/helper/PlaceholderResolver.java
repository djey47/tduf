package fr.tduf.libunlimited.high.files.common.patcher.helper;

import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;

import java.util.regex.Pattern;

/**
 * Parent class for all resolvers
 */
public abstract class PlaceholderResolver {
    protected static final Pattern PATTERN_PLACEHOLDER = Pattern.compile("\\{(.+)}");                         // e.g {FOO}

    protected PatchProperties patchProperties;

    /**
     * Main component entry point
     */
    public abstract void resolveAllPlaceholders();
}
