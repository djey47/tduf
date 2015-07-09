package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class BitfieldHelperTest {

    @Test
    public void newBitFieldHelper_shouldLoadDatabaseMetadata() throws IOException, URISyntaxException {
        // GIVEN-WHEN
        BitfieldHelper bitfieldHelper = new BitfieldHelper();

        // THEN
        assertThat(bitfieldHelper.getDatabaseMetadataObject()).isNotNull();
    }

    @Test
    public void getBitfieldReferenceForTopic_whenUnavailable_shouldReturnEmpty() throws Exception {
        // GIVEN-WHEN
        Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> bitfieldReferenceForTopic = new BitfieldHelper().getBitfieldReferenceForTopic(DbDto.Topic.TUTORIALS);

        // THEN
        assertThat(bitfieldReferenceForTopic).isEmpty();
    }

    @Test
    public void getBitfieldReferenceForTopic_whenAvailable_shouldReturnIt() throws Exception {
        // GIVEN-WHEN
        Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> bitfieldReferenceForTopic = new BitfieldHelper().getBitfieldReferenceForTopic(DbDto.Topic.CAR_PHYSICS_DATA);

        // THEN
        assertThat(bitfieldReferenceForTopic).isPresent();
    }

    @Test(expected = NullPointerException.class)
    public void resolve_wheNullRawValue_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN - WHEN
        new BitfieldHelper().resolve(DbDto.Topic.CAR_PHYSICS_DATA, null);

        // THEN: NPE
    }

    @Test
    public void resolve_whenNoReference_shouldReturnEmpty() throws IOException, URISyntaxException {
        // GIVEN - WHEN
        Optional<List<Boolean>> resolved = new BitfieldHelper().resolve(DbDto.Topic.TUTORIALS, "101");

        // THEN
        assertThat(resolved).isEmpty();
    }

    @Test
    public void resolve_whenReference_shouldReturnResolvedSwitches() throws IOException, URISyntaxException {
        // GIVEN - WHEN
        List<Boolean> resolved = new BitfieldHelper().resolve(DbDto.Topic.CAR_PHYSICS_DATA, "101").get();

        // THEN
        assertThat(resolved).hasSize(7);
        assertThat(resolved).containsExactly(true, false, true, false, false, true, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolve_whenRawValueHasIllegalFormat_shouldThrowException() throws IOException, URISyntaxException {
        // GIVEN - WHEN
        new BitfieldHelper().resolve(DbDto.Topic.CAR_PHYSICS_DATA, "abc").get();

        // THEN: IAE
    }
}