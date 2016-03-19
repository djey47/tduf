package fr.tduf.libunlimited.high.files.db.patcher.dto.comparator;

import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;
import org.junit.Test;

import static fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto.DbChangeDto.ChangeTypeEnum.*;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_PHYSICS_DATA;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_RIMS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.TUTORIALS;
import static fr.tduf.libunlimited.low.files.db.dto.DbResourceDto.Locale.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class DbChangeDtoRenderComparatorTest {

    private DbChangeDtoRenderComparator renderComparator = new DbChangeDtoRenderComparator();

    @Test
    public void compare_topic_against_same_topic_returns_0() {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject1 = createChangeObjectForCarPhysics(DELETE);
        DbPatchDto.DbChangeDto deleteChangeObject2 = createChangeObjectForCarPhysics(DELETE);

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject1, deleteChangeObject2);

        // THEN
        assertThat(compareResult).isZero();
    }

    @Test
    public void compare_topic_against_different_topic_returns_positive() {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject1 = createChangeObject(DELETE, TUTORIALS);
        DbPatchDto.DbChangeDto deleteChangeObject2 = createChangeObject(DELETE, CAR_PHYSICS_DATA);

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject1, deleteChangeObject2);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_topic_against_different_topic_returns_negative() {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject = createChangeObject(DELETE, CAR_PHYSICS_DATA);
        DbPatchDto.DbChangeDto deleteResourceChangeObject = createChangeObject(DELETE_RES, TUTORIALS);

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject, deleteResourceChangeObject);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_delete_against_delete_returns_0() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject1 = createChangeObjectForCarPhysics(DELETE);
        DbPatchDto.DbChangeDto deleteChangeObject2 = createChangeObjectForCarPhysics(DELETE);

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject1, deleteChangeObject2);

        // THEN
        assertThat(compareResult).isZero();
    }

    @Test
    public void compare_delete_against_deleteResource_returns_negative() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject = createChangeObjectForCarPhysics(DELETE);
        DbPatchDto.DbChangeDto deleteResourceChangeObject = createChangeObjectForCarPhysics(DELETE_RES);

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject, deleteResourceChangeObject);

        // THEN
        assertThat(compareResult).isNegative();
    }
    @Test
    public void compare_delete_against_update_returns_negative() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject = createChangeObjectForCarPhysics(DELETE);
        DbPatchDto.DbChangeDto updateChangeObject = createChangeObjectForCarPhysics(UPDATE);

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject, updateChangeObject);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_delete_against_updateResource_returns_negtive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject = createChangeObjectForCarPhysics(DELETE);
        DbPatchDto.DbChangeDto updateResourceChangeObject = createChangeObjectForCarPhysics(UPDATE_RES);

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject, updateResourceChangeObject);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_deleteResource_against_delete_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject = createChangeObjectForCarPhysics(DELETE);
        DbPatchDto.DbChangeDto deleteResourceChangeObject = createChangeObjectForCarPhysics(DELETE_RES);

        // WHEN
        int compareResult = renderComparator.compare(deleteResourceChangeObject, deleteChangeObject);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_update_against_deleteResource_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject = createChangeObjectForCarPhysics(UPDATE);
        DbPatchDto.DbChangeDto deleteResourceChangeObject = createChangeObjectForCarPhysics(DELETE_RES);

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject, deleteResourceChangeObject);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_update_against_updateResource_returns_negative() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject = createChangeObjectForCarPhysics(UPDATE);
        DbPatchDto.DbChangeDto updateResourceChangeObject = createChangeObjectForCarPhysics(UPDATE_RES);

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject, updateResourceChangeObject);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_updateResource_against_deleteResource_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateResourceChangeObject = createChangeObjectForCarPhysics(UPDATE_RES);
        DbPatchDto.DbChangeDto deleteResourceChangeObject = createChangeObjectForCarPhysics(DELETE_RES);

        // WHEN
        int compareResult = renderComparator.compare(updateResourceChangeObject, deleteResourceChangeObject);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_updateResource_against_update_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateResourceChangeObject = createChangeObjectForCarPhysics(UPDATE_RES);
        DbPatchDto.DbChangeDto updateChangeObject = createChangeObjectForCarPhysics(UPDATE);

        // WHEN
        int compareResult = renderComparator.compare(updateResourceChangeObject, updateChangeObject);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_noReference_against_anyReference_returns_negative() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysics(UPDATE);
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRef(UPDATE, "10010101");

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_anyReference_against_NoReference_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysics(UPDATE);
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRef(UPDATE, "10010101");

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject2, updateChangeObject1);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_reference_against_reference_returns_negative() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysicsWithRef(UPDATE, "1000");
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRef(UPDATE, "1001");

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_reference_against_same_reference_returns_0() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysicsWithRef(UPDATE, "1000");
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRef(UPDATE, "1000");

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isZero();
    }

    @Test
    public void compare_reference_against_reference_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysicsWithRef(UPDATE, "1001");
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRef(UPDATE, "1000");

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_noLocale_against_anyLocale_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysicsWithRef(UPDATE_RES, "10010101");
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRefForLocale(UPDATE, "10010101", FRANCE);

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_anyLocale_against_NoLocale_returns_negative() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysicsWithRef(UPDATE_RES, "10010101");
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRefForLocale(UPDATE, "10010101", FRANCE);

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject2, updateChangeObject1);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_locale_against_locale_returns_negative() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysicsWithRefForLocale(UPDATE, "1000", FRANCE );
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRefForLocale(UPDATE, "1001", UNITED_STATES);

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_locale_against_same_locale_returns_0() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysicsWithRefForLocale(UPDATE, "1000", KOREA);
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRefForLocale(UPDATE, "1000", KOREA);

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isZero();
    }

    @Test
    public void compare_locale_against_locale_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectForCarPhysicsWithRefForLocale(UPDATE, "1001", JAPAN);
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectForCarPhysicsWithRefForLocale(UPDATE, "1000", CHINA);

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_filter_against_filter_returns_negative() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject1 = createChangeObjectForCarPhysicsWithFilter(DELETE, DbFieldValueDto.fromCouple(1, "1000"));
        DbPatchDto.DbChangeDto deleteChangeObject2 = createChangeObjectForCarPhysicsWithFilter(DELETE, DbFieldValueDto.fromCouple(1, "1001"));

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject1, deleteChangeObject2);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_filter_against_same_filter_returns_zero() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject1 = createChangeObjectForCarPhysicsWithFilter(DELETE, DbFieldValueDto.fromCouple(1, "1000"));
        DbPatchDto.DbChangeDto deleteChangeObject2 = createChangeObjectForCarPhysicsWithFilter(DELETE, DbFieldValueDto.fromCouple(1, "1000"));

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject1, deleteChangeObject2);

        // THEN
        assertThat(compareResult).isZero();
    }

    @Test
    public void compare_filter_against_filter_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto deleteChangeObject1 = createChangeObjectForCarPhysicsWithFilter(DELETE, DbFieldValueDto.fromCouple(1, "1001"));
        DbPatchDto.DbChangeDto deleteChangeObject2 = createChangeObjectForCarPhysicsWithFilter(DELETE, DbFieldValueDto.fromCouple(1, "1000"));

        // WHEN
        int compareResult = renderComparator.compare(deleteChangeObject1, deleteChangeObject2);

        // THEN
        assertThat(compareResult).isPositive();
    }

    @Test
    public void compare_values_against_values_returns_negative() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectWithEntryValues(UPDATE, CAR_RIMS, "1000", "2000");
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectWithEntryValues(UPDATE, CAR_RIMS, "1001", "1999");

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isNegative();
    }

    @Test
    public void compare_values_against_same_values_returns_zero() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectWithEntryValues(UPDATE, CAR_RIMS, "1000", "2000");
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectWithEntryValues(UPDATE, CAR_RIMS, "1000", "1999");

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isZero();
    }

    @Test
    public void compare_values_against_values_returns_positive() throws Exception {
        // GIVEN
        DbPatchDto.DbChangeDto updateChangeObject1 = createChangeObjectWithEntryValues(UPDATE, CAR_RIMS, "1001", "1999");
        DbPatchDto.DbChangeDto updateChangeObject2 = createChangeObjectWithEntryValues(UPDATE, CAR_RIMS, "1000", "2000");

        // WHEN
        int compareResult = renderComparator.compare(updateChangeObject1, updateChangeObject2);

        // THEN
        assertThat(compareResult).isPositive();
    }

    private static DbPatchDto.DbChangeDto createChangeObjectForCarPhysicsWithRefForLocale(DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, String ref, DbResourceDto.Locale locale) {
        return DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_PHYSICS_DATA)
                .withType(changeType)
                .asReference(ref)
                .forLocale(locale)
                .build();
    }

    private static DbPatchDto.DbChangeDto createChangeObjectForCarPhysicsWithFilter(DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, DbFieldValueDto... fieldValues) {
        return DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_PHYSICS_DATA)
                .withType(changeType)
                .filteredBy(asList(fieldValues))
                .build();
    }

    private static DbPatchDto.DbChangeDto createChangeObjectForCarPhysicsWithRef(DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, String ref) {
        return DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_PHYSICS_DATA)
                .withType(changeType)
                .asReference(ref)
                .build();
    }

    private static DbPatchDto.DbChangeDto createChangeObjectForCarPhysics(DbPatchDto.DbChangeDto.ChangeTypeEnum changeType) {
        return DbPatchDto.DbChangeDto.builder()
                .forTopic(CAR_PHYSICS_DATA)
                .withType(changeType)
                .build();
    }

    private static DbPatchDto.DbChangeDto createChangeObject(DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, DbDto.Topic topic) {
        return DbPatchDto.DbChangeDto.builder()
                .forTopic(topic)
                .withType(changeType)
                .build();
    }

    private static DbPatchDto.DbChangeDto createChangeObjectWithEntryValues(DbPatchDto.DbChangeDto.ChangeTypeEnum changeType, DbDto.Topic topic, String... entryValues) {
        return DbPatchDto.DbChangeDto.builder()
                .forTopic(topic)
                .withType(changeType)
                .withEntryValues(asList(entryValues))
                .build();
    }
}
