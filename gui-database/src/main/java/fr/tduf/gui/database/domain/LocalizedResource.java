package fr.tduf.gui.database.domain;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.util.Pair;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * Represents a browsed/selected resource with optional localization and topic information.
 */
public class LocalizedResource {

    private final Pair<String, String> referenceValuePair;

    private Locale locale;

    private DbDto.Topic topic;

    public LocalizedResource(Pair<String, String> referenceValuePair, Locale locale) {
        this.referenceValuePair = referenceValuePair;
        this.locale = locale;
    }

    public LocalizedResource(DbDto.Topic topic, String reference) {
        this.topic = requireNonNull(topic, "Null forbidden for database topic");
        this.referenceValuePair = new Pair<>(reference, "");
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    public Pair<String, String> getReferenceValuePair() {
        return referenceValuePair;
    }

    public String getReference() {
        return referenceValuePair.getKey();
    }

    public Optional<Locale> getLocale() {
        return ofNullable(locale);
    }

    public Optional<DbDto.Topic> getTopic() {
        return ofNullable(topic);
    }
}