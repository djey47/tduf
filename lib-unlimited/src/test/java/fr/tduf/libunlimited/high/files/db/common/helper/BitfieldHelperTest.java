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
        Optional<List<DbMetadataDto.TopicMetadataDto.BitfieldMetadataDto>> bitfieldReferenceForTopic = new BitfieldHelper().getBitfieldReferenceForTopic(DbDto.Topic.PNJ);

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
}