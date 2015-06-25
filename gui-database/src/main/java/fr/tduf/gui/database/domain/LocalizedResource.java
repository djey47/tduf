package fr.tduf.gui.database.domain;

import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.util.Pair;

import java.util.Optional;

/**
 * Represents a selected resource with optional localization information.
 */
// TODO simplify (3 classes for resources ...)
public class LocalizedResource {

    private Pair<String, String> referenceValuePair;

    private Optional<DbResourceDto.Locale> locale;

    public LocalizedResource(Pair<String, String> referenceValuePair, Optional<DbResourceDto.Locale> locale) {
        this.referenceValuePair = referenceValuePair;
        this.locale = locale;
    }

    public Pair<String, String> getReferenceValuePair() {
        return referenceValuePair;
    }

    public Optional<DbResourceDto.Locale> getLocale() {
        return locale;
    }
}