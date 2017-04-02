package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BitfieldHelperTest {

    private BitfieldHelper bitfieldHelper;

    @BeforeEach
    void setUp() throws Exception {
        bitfieldHelper = new BitfieldHelper();
    }

    @Test
    void getBitfieldReferenceForTopic_whenUnavailable_shouldReturnEmpty() throws Exception {
        // GIVEN-WHEN
        Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> bitfieldReferenceForTopic = bitfieldHelper.getBitfieldReferenceForTopic(DbDto.Topic.TUTORIALS);

        // THEN
        assertThat(bitfieldReferenceForTopic).isEmpty();
    }

    @Test
    void getBitfieldReferenceForTopic_whenAvailable_shouldReturnIt() throws Exception {
        // GIVEN-WHEN
        Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> bitfieldReferenceForTopic = bitfieldHelper.getBitfieldReferenceForTopic(DbDto.Topic.CAR_PHYSICS_DATA);

        // THEN
        assertThat(bitfieldReferenceForTopic).isPresent();
    }

    @Test
    void resolve_wheNullRawValue_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN - WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> bitfieldHelper.resolve(DbDto.Topic.CAR_PHYSICS_DATA, null));
    }

    @Test
    void resolve_whenNoReference_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN - WHEN
        Optional<List<Boolean>> resolved = bitfieldHelper.resolve(DbDto.Topic.TUTORIALS, "101");

        // THEN
        assertThat(resolved).isEmpty();
    }

    @Test
    void resolve_whenReference_shouldReturnResolvedSwitches() throws IOException, URISyntaxException {
        // GIVEN - WHEN
        List<Boolean> resolved = bitfieldHelper.resolve(DbDto.Topic.CAR_PHYSICS_DATA, "101").get();

        // THEN
        assertThat(resolved).hasSize(7);
        assertThat(resolved).containsExactly(true, false, true, false, false, true, true);
    }

    @Test
    void resolve_whenRawValueHasIllegalFormat_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN - WHEN - THEN
        assertThrows(IllegalArgumentException.class,
                () -> bitfieldHelper.resolve(DbDto.Topic.CAR_PHYSICS_DATA, "abc").get());
    }

    @Test
    void updateRawValue_whenNoReference_shouldReturnEmpty() {
        // GIVEN-WHEN
        Optional<String> actualValue = bitfieldHelper.updateRawValue(DbDto.Topic.CAR_RIMS, "111", 1, false);

        // THEN
        assertThat(actualValue).isEmpty();
    }

    @Test
    void updateRawValue_whenReference_shouldReturnValueWithChangedBitState() {
        // GIVEN-WHEN
        String actualValue = bitfieldHelper.updateRawValue(DbDto.Topic.CAR_PHYSICS_DATA, "111", 1, false).get();

        // THEN
        assertThat(actualValue).isEqualTo("110");
    }

    @Test
    void updateRawValue_whenReference_andBitIndexOutOfBounds_shouldReturnInitialValue() {
        // GIVEN-WHEN
        String actualValue = bitfieldHelper.updateRawValue(DbDto.Topic.CAR_PHYSICS_DATA, "111", 50, true).get();

        // THEN
        assertThat(actualValue).isEqualTo("111");
    }

    @Test
    void updateRawValue_whenRawValueHasIllegalFormat_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN - WHEN - THEN
        assertThrows(IllegalArgumentException.class,
                () -> bitfieldHelper.updateRawValue(DbDto.Topic.CAR_PHYSICS_DATA, "abc", 1, false));
    }
}