package fr.tduf.gui.database.domain;

import com.google.common.base.MoreObjects;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import javafx.util.Pair;

import java.util.Optional;

/**
 * Represents a browsed/selected resource with optional localization and topic information.
 */
public class LocalizedResource {

    private Pair<String, String> referenceValuePair;

    private Optional<DbResourceDto.Locale> locale;

    private Optional<DbDto.Topic> topic;

    public LocalizedResource(Pair<String, String> referenceValuePair, Optional<DbResourceDto.Locale> locale) {
        this.referenceValuePair = referenceValuePair;
        this.locale = locale;
    }

    public LocalizedResource(DbDto.Topic topic, String reference) {
        this.topic = Optional.of(topic);
        this.referenceValuePair = new Pair<>(reference, "");
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topic", topic)
                .add("locale", locale)
                .add("referenceValuePair", referenceValuePair)
                .toString();
    }

    public Pair<String, String> getReferenceValuePair() {
        return referenceValuePair;
    }

    public String getReference() {
        return referenceValuePair.getKey();
    }

    public Optional<DbResourceDto.Locale> getLocale() {
        return locale;
    }

    public Optional<DbDto.Topic> getTopic() {
        return topic;
    }
}