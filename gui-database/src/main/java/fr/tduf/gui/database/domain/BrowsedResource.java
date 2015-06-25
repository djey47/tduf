package fr.tduf.gui.database.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;

/**
 * Describes a resource (globalized ou localized) to browse via dedicated stage.
 */
// TODO simplify (3 classes for resources ...)
public class BrowsedResource {
    private DbDto.Topic topic;
    private String reference;

    public BrowsedResource(DbDto.Topic topic, String reference) {
        this.topic = topic;
        this.reference = reference;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topic", topic)
                .add("reference", reference)
                .toString();
    }

    public DbDto.Topic getTopic() {
        return topic;
    }

    public String getReference() {
        return reference;
    }
}