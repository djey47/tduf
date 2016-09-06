package fr.tduf.gui.database.common;

import fr.tduf.libunlimited.common.game.domain.Locale;

import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;

/**
 * Gives all constants needed to default settings.
 */
public class SettingsConstants {
    public static final String DATABASE_DIRECTORY_DEFAULT = "";

    public static final String PATH_RESOURCE_PROFILES = "/gui-database/layout/defaultProfiles.json";

    public static  final Locale DEFAULT_LOCALE = UNITED_STATES;

    private SettingsConstants(){}
}
