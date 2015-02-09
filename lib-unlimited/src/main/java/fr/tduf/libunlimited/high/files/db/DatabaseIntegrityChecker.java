package fr.tduf.libunlimited.high.files.db;

import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Class providing methods to check Database integrity.
 */
public class DatabaseIntegrityChecker {

    private final List<DbDto> dbDtos;

    private Map<DbDto.Topic, List<DbResourceDto>> resourcesByTopic;

    private Map<String, DbDto.Topic> topicsByReferences;

    private DatabaseIntegrityChecker(List<DbDto> dbDtos) {
        this.dbDtos = dbDtos;

        buildIndexes();
    }

    /**
     * Single entry point for this checker.
     * @param dbDtos    : per topic, database objects
     * @return a {@link DatabaseIntegrityChecker} instance.
     */
    public static DatabaseIntegrityChecker load(List<DbDto> dbDtos) {
        checkRequirements(dbDtos);

        return new DatabaseIntegrityChecker(dbDtos);
    }

    /**
     * Process checkng over all loaded database objects.
     * @return list of integrity errors.
     */
    // TODO simplify!
    public List<IntegrityError> checkAll() {

        List<IntegrityError> integrityErrors = new ArrayList<>();

        dbDtos.stream()
                
                .forEach( (dto) -> {

                    Map<String, DbStructureDto.Field> fieldsByNames = buildFieldIndex(dto);

                    dto.getData().getEntries().stream()

                            .forEach((entry) -> entry.getItems().stream()

                                    .forEach((item) -> {

                                        DbDto.Topic topic = dto.getStructure().getTopic();
                                        DbStructureDto.Field field = fieldsByNames.get(item.getName());

                                        switch (field.getFieldType()) {
                                            case RESOURCE_CURRENT:
                                                integrityErrors.addAll(checkResourceReference(item.getRawValue(), topic));
                                                break;
                                            case RESOURCE_REMOTE:
                                                DbDto.Topic remoteTopic = topicsByReferences.get(field.getTargetRef());
                                                integrityErrors.addAll(checkResourceReference(item.getRawValue(), remoteTopic));
                                                break;
                                            default:
                                                break;
                                        }


                                    }));
                });

       return integrityErrors;
    }

    private static void checkRequirements(List<DbDto> dbDtos) {
        requireNonNull(dbDtos, "A list of database objects is required.");

        List<DbDto.Topic> presentTopics = dbDtos.stream()

                .map((dto) -> dto.getStructure().getTopic())

                .collect(toList());

        List<DbDto.Topic> absentTopics = asList(DbDto.Topic.values()).stream()

                .filter((topicEnum) -> !presentTopics.contains(topicEnum))

                .collect(toList());

        if (!absentTopics.isEmpty()) {
            throw new IllegalArgumentException("Missing one or more database topics: " + absentTopics);
        }
    }

    private List<IntegrityError> checkResourceReference(String reference, DbDto.Topic topic) {
        List<IntegrityError> integrityErrors = new ArrayList<>();

        // Through all language resources
        List<DbResourceDto> dbResourceDtos = resourcesByTopic.get(topic);

        for(DbResourceDto resourceDto : dbResourceDtos) {

            List<String> availableReferences = resourceDto.getEntries().stream()

                    .map(DbResourceDto.Entry::getReference)

                    .collect(toList());

            if (!availableReferences.contains(reference)) {
                Map<String, Object> informations = new HashMap<>();
                informations.put("Topic", topic);
                informations.put("Locale", resourceDto.getLocale());
                informations.put("Reference", reference);

                integrityErrors.add(IntegrityError.builder()
                                .ofType(IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND)
                                .addInformations(informations)
                                .build()
                );
            }
        }

        return integrityErrors;
    }

    private void buildIndexes() {

        // Resource list by topic
        resourcesByTopic = dbDtos.stream()

                .collect(toMap((dto) -> dto.getStructure().getTopic(), DbDto::getResources));

        // Topic by reference
        topicsByReferences = dbDtos.stream()

                .collect(toMap((dto) -> dto.getStructure().getRef(), (dto) -> dto.getStructure().getTopic()));
    }

    private Map<String, DbStructureDto.Field> buildFieldIndex(DbDto dto) {
        return dto.getStructure().getFields().stream()

                .collect(toMap(DbStructureDto.Field::getName, (field) -> field));
    }

    Map<DbDto.Topic, List<DbResourceDto>> getResourcesByTopic() {
        return resourcesByTopic;
    }

    Map<String, DbDto.Topic> getTopicsByReferences() {
        return topicsByReferences;
    }
}