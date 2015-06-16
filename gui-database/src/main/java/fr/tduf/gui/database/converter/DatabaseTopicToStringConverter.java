package fr.tduf.gui.database.converter;

import fr.tduf.gui.database.common.DisplayConstants;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import javafx.util.StringConverter;

public class DatabaseTopicToStringConverter extends StringConverter<DbDto.Topic> {
    @Override
    public String toString(DbDto.Topic topic) {
        if (topic == null) {
            return DisplayConstants.LABEL_ITEM_TOPIC_DEFAULT;
        }
        return topic.name();
    }

    @Override
    public DbDto.Topic fromString(String topicName) {
        return DbDto.Topic.valueOf(topicName);
    }
}
