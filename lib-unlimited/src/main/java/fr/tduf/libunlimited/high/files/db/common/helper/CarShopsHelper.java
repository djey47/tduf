package fr.tduf.libunlimited.high.files.db.common.helper;

import fr.tduf.libunlimited.high.files.db.dto.DbMetadataDto;

import java.util.Optional;

/**
 * Helper class to access more information about vehicle dealers.
 */
public class CarShopsHelper extends MetaDataHelper {
    /**
     * @param dealerReference : REF value of dealer entry
     * @return reference for this dealer if available, otherwise empty.
     */
    public Optional<DbMetadataDto.DealerMetadataDto> getCarShopsReferenceForDealerReference(String dealerReference) {
        return databaseMetadataObject.getDealers().stream()

                .filter((dealerMetaData) -> dealerMetaData.getReference().equals(dealerReference))

                .findAny();
    }
}
