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

    private Map<String, DbDto> topicObjectssByReferences;

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

                                        DbDto.Topic currentTopic = dto.getStructure().getTopic();
                                        DbDto remoteTopicObject;
                                        DbStructureDto.Field field = fieldsByNames.get(item.getName());

                                        switch (field.getFieldType()) {
                                            case REFERENCE:
                                                remoteTopicObject = topicObjectssByReferences.get(field.getTargetRef());
                                                integrityErrors.addAll(checkContentsReference(item.getRawValue(), remoteTopicObject, currentTopic));
                                                break;
                                            case RESOURCE_CURRENT:
                                                integrityErrors.addAll(checkResourceReference(item.getRawValue(), dto, currentTopic));
                                                break;
                                            case RESOURCE_REMOTE:
                                                remoteTopicObject = topicObjectssByReferences.get(field.getTargetRef());
                                                integrityErrors.addAll(checkResourceReference(item.getRawValue(), remoteTopicObject, currentTopic));
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

        List<DbDto.Topic> absentTopics = asList(DbDto.Topic.values()).stream()

                .filter((topicEnum) -> dbDtos.stream()

                        .map((dto) -> dto.getStructure().getTopic())

                        .filter((topic) -> topic == topicEnum)

                        .count() == 0)

                .collect(toList());

        if (!absentTopics.isEmpty()) {
            throw new IllegalArgumentException("Missing one or more database topics: " + absentTopics);
        }
    }

    private List<IntegrityError> checkResourceReference(String reference, DbDto topicObject, DbDto.Topic sourceTopic) {
        List<IntegrityError> integrityErrors = new ArrayList<>();

        // Through all language resources
        List<DbResourceDto> dbResourceDtos = topicObject.getResources();

        for(DbResourceDto resourceDto : dbResourceDtos) {

            List<String> availableReferences = resourceDto.getEntries().stream()

                    .map(DbResourceDto.Entry::getReference)

                    .collect(toList());

            if (!availableReferences.contains(reference)) {
                Map<String, Object> informations = new HashMap<>();
                informations.put("Source Topic", sourceTopic);
                informations.put("Remote Topic", topicObject.getStructure().getTopic());
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

    private List<IntegrityError> checkContentsReference(String reference, DbDto topicObject, DbDto.Topic sourceTopic) {
        List<IntegrityError> integrityErrors = new ArrayList<>();

        Map<String, DbStructureDto.Field> fieldIndex = buildFieldIndex(topicObject);
        boolean isReferenceFound = topicObject.getData().getEntries().stream()

                .filter((entry) -> entry.getItems().stream()

                                .filter((item) ->
                                        fieldIndex.get(item.getName()).getFieldType() == DbStructureDto.FieldType.UID
                                                && item.getRawValue().equals(reference))

                                .findFirst()

                                .isPresent()
                )

                .findFirst()

                .isPresent();

        if (!isReferenceFound) {

            Map<String, Object> informations = new HashMap<>();
            informations.put("Source Topic", sourceTopic);
            informations.put("Remote Topic", topicObject.getStructure().getTopic());
            informations.put("Reference", reference);

            integrityErrors.add(IntegrityError.builder()
                            .ofType(IntegrityError.ErrorTypeEnum.CONTENTS_REFERENCE_NOT_FOUND)
                            .addInformations(informations)
                            .build()
            );
        }

        return integrityErrors;
    }

    private void buildIndexes() {

        // Topic by reference
        topicObjectssByReferences = dbDtos.stream()

                .collect( toMap((dto) -> dto.getStructure().getRef(), (dto) -> dto) );
    }

    private Map<String, DbStructureDto.Field> buildFieldIndex(DbDto dto) {
        return dto.getStructure().getFields().stream()

                .collect(toMap(DbStructureDto.Field::getName, (field) -> field));
    }

    Map<String, DbDto> getTopicObjectssByReferences() {
        return topicObjectssByReferences;
    }
}