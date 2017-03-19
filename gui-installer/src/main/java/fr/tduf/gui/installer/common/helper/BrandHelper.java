package fr.tduf.gui.installer.common.helper;

import fr.tduf.libunlimited.high.files.db.common.DatabaseConstants;
import fr.tduf.gui.installer.domain.Brand;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static java.util.stream.Collectors.toList;

/**
 * Component to get advanced information on vehicle brands.
 */
public class BrandHelper extends CommonHelper {
    private BrandHelper(BulkDatabaseMiner miner) {
        super(miner);
    }

    /**
     * @param miner : component to parse database
     * @return a new helper instance.
     */
    public static BrandHelper load(BulkDatabaseMiner miner) {
        return new BrandHelper(miner);
    }

    /**
     * @param brandReference    : entry reference (UID)
     * @return brand information about provided reference, or empty if none has been found.
     */
    public Optional<Brand> getBrandFromReference(String brandReference) {
        return miner.getContentEntryFromTopicWithReference(brandReference, BRANDS)
                .map(this::brandEntryToDomainObject);
    }

    /**
     * @param criteria    : manufacturer id or displayed name
     * @return any brand information about provided criteria (case-insensitive), or empty if none has been found.
     */
    public Optional<Brand> getBrandFromIdentifierOrName(String criteria) {
        return getAllBrandsStream()
                .filter(brand -> brand.getIdentifier().getValue().equalsIgnoreCase(criteria)
                        || brand.getDisplayedName().getValue().equalsIgnoreCase(criteria))
                .findAny();
    }

    /**
     * @return all available brands in database
     */
    public List<Brand> getAllBrands() {
        return getAllBrandsStream()
                .collect(toList());
    }

    private Stream<Brand> getAllBrandsStream() {
        return miner.getDatabaseTopic(BRANDS)
                .orElseThrow(() -> new IllegalStateException("No brands information was found in database"))
                .getData().getEntries().stream()
                .map(this::brandEntryToDomainObject);
    }

    private Brand brandEntryToDomainObject(ContentEntryDto brandEntry) {
        String brandRef = brandEntry.getItemAtRank(DatabaseConstants.FIELD_RANK_BRAND_REF)
                .orElseThrow(() -> new IllegalStateException("No item at rank 1 in brands topic"))
                .getRawValue();
        Resource idResource = getResourceFromDatabaseEntry(brandEntry, BRANDS, DatabaseConstants.FIELD_RANK_MANUFACTURER_ID)
                .orElseThrow(() -> new IllegalStateException("No manufacturer identifier"));
        Resource nameResource = getResourceFromDatabaseEntry(brandEntry, BRANDS, DatabaseConstants.FIELD_RANK_MANUFACTURER_NAME)
                .orElseThrow(() -> new IllegalStateException("No manufacturerbrand name"));

        return Brand.builder()
                .withReference(brandRef)
                .withIdentifier(idResource)
                .withDisplayedName(nameResource)
                .build();
    }
}
