package fr.tduf.libunlimited.high.files.db.integrity;

import fr.tduf.libunlimited.low.files.db.domain.IntegrityError;
import fr.tduf.libunlimited.low.files.db.dto.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.DbStructureDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.CONTENTS_REFERENCE_NOT_FOUND;
import static fr.tduf.libunlimited.low.files.db.domain.IntegrityError.ErrorTypeEnum.RESOURCE_REFERENCE_NOT_FOUND;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Class providing methods to check Database integrity.
 */
public class DatabaseIntegrityChecker {

    private final List<DbDto> dbDtos;

    private Map<String, DbDto> topicObjectsByReferences;
    private Map<DbDto, Map<Integer, DbStructureDto.Field>> fieldsByRanksByTopicObjects;

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
     * Process checking over all loaded database objects.
     * @return list of integrity errors.
     */
    //TODO long method - optimize it
    public List<IntegrityError> checkAllContentsObjects() {

        List<IntegrityError> integrityErrors = new ArrayList<>();

        dbDtos.stream()

                .forEach((localTopicObject) -> checkContentsObject(localTopicObject, integrityErrors));

       return integrityErrors;
    }

    private static void checkRequirements(List<DbDto> dbDtos) {
        requireNonNull(dbDtos, "A list of database objects is required.");

        checkIfAllTopicObjectsPresent(dbDtos);
    }

    private static void checkIfAllTopicObjectsPresent(List<DbDto> dbDtos) {
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

    private void checkContentsObject(DbDto contentsObject, List<IntegrityError> integrityErrors) {

        Map<Integer, DbStructureDto.Field> fieldsByRankIndex = fieldsByRanksByTopicObjects.get(contentsObject);

        contentsObject.getData().getEntries().stream()

                .forEach((entry) -> entry.getItems().stream()

                        .forEach((item) -> checkContentsItem(item, contentsObject, fieldsByRankIndex, integrityErrors)));
    }

    private void checkContentsItem(DbDataDto.Item item, DbDto localTopicObject, Map<Integer, DbStructureDto.Field> fieldsByRanksIndex, List<IntegrityError> integrityErrors) {
        DbStructureDto.Field field = fieldsByRanksIndex.get(item.getFieldRank());
        String targetRef = field.getTargetRef();

        DbDto remoteTopicObject = null;
        if (targetRef != null) {
            remoteTopicObject = topicObjectsByReferences.get(targetRef);
        }

        DbDto.Topic currentTopic = localTopicObject.getStructure().getTopic();

        switch (field.getFieldType()) {
            case REFERENCE:
                integrityErrors.addAll(checkContentsReference(item.getRawValue(), remoteTopicObject, currentTopic));
                break;
            case RESOURCE_CURRENT:
            case RESOURCE_CURRENT_AGAIN:
                integrityErrors.addAll(checkResourceReference(item.getRawValue(), localTopicObject, currentTopic));
                break;
            case RESOURCE_REMOTE:
                integrityErrors.addAll(checkResourceReference(item.getRawValue(), remoteTopicObject, currentTopic));
                break;
            default:
                break;
        }
    }

    private List<IntegrityError> checkResourceReference(String reference, DbDto topicObject, DbDto.Topic sourceTopic) {
        List<IntegrityError> integrityErrors = new ArrayList<>();

        // Through all language resources

        topicObject.getResources().stream()

                .parallel()

                .forEach((resourceDto) -> {

                    boolean isResourceReferenceFound = resourceDto.getEntries().stream()

                            .map(DbResourceDto.Entry::getReference)

                            .filter((aReference) -> aReference.equals(reference))

                            .findFirst()

                            .isPresent();

                    if (!isResourceReferenceFound) {
                        Map<String, Object> informations = new HashMap<>();
                        informations.put("Source Topic", sourceTopic);
                        informations.put("Remote Topic", topicObject.getStructure().getTopic());
                        informations.put("Locale", resourceDto.getLocale());
                        informations.put("Reference", reference);

                        integrityErrors.add(IntegrityError.builder()
                                        .ofType(RESOURCE_REFERENCE_NOT_FOUND)
                                        .addInformations(informations)
                                        .build()
                        );
                    }

                });

        return integrityErrors;
    }

    private List<IntegrityError> checkContentsReference(String reference, DbDto topicObject, DbDto.Topic sourceTopic) {
        Map<Integer, DbStructureDto.Field> fieldsbyRanks = fieldsByRanksByTopicObjects.get(topicObject);

        List<IntegrityError> integrityErrors = new ArrayList<>();

        boolean isReferenceFound = topicObject.getData().getEntries().stream()

                .parallel()

                .filter((entry) -> entry.getItems().stream()

                                .parallel()

                                .filter((item) -> fieldsbyRanks
                                        .get(item.getFieldRank())
                                        .getFieldType() == DbStructureDto.FieldType.UID
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
                            .ofType(CONTENTS_REFERENCE_NOT_FOUND)
                            .addInformations(informations)
                            .build()
            );
        }

        return integrityErrors;
    }

    private void buildIndexes() {

        // Topic by reference
        topicObjectsByReferences = dbDtos.stream()

                .collect( toMap((dto) -> dto.getStructure().getRef(), (dto) -> dto) );

        // Structure
        fieldsByRanksByTopicObjects = dbDtos.stream()

                .collect(toMap((dto) -> dto, this::buildFieldIndex));
    }

    private Map<Integer, DbStructureDto.Field> buildFieldIndex(DbDto dto) {
        return dto.getStructure().getFields().stream()

                .collect(toMap(DbStructureDto.Field::getRank, (field) -> field));
    }

    Map<String, DbDto> getTopicObjectsByReferences() {
        return topicObjectsByReferences;
    }

    Map<DbDto, Map<Integer, DbStructureDto.Field>> getFieldsByRanksByTopicObjects() {
        return fieldsByRanksByTopicObjects;
    }
}