package fr.tduf.gui.installer.common.helper;

import fr.tduf.gui.installer.domain.Brand;
import fr.tduf.gui.installer.domain.Resource;
import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.content.ContentItemDto;
import fr.tduf.libunlimited.low.files.db.dto.content.DbDataDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceItemDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static fr.tduf.gui.installer.domain.Resource.from;
import static fr.tduf.libunlimited.common.game.domain.Locale.UNITED_STATES;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.BRANDS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrandHelperTest {
    private static final String BRAND_REF_1 = "REF1";
    private static final String BRAND_REF_2 = "REF2";
    private static final String BRAND_NOREF = "REF3";
    private static final Resource BRAND_ID_RESOURCE_1 = from("ID1", "ALFA");
    private static final Resource BRAND_NAME_RESOURCE_1 = from("NAME1", "Alfa Romeo");
    private static final Resource BRAND_ID_RESOURCE_2 = from("ID2", "HONDA");
    private static final Resource BRAND_NAME_RESOURCE_2 = from("NAME2", "Honda");

    @Mock
    private BulkDatabaseMiner miner;

    @InjectMocks
    private BrandHelper brandHelper;

    @Before
    public void setUp() {
        ContentEntryDto brandsEntry1 = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(BRAND_REF_1).build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(BRAND_ID_RESOURCE_1.getRef()).build())
                .addItem(ContentItemDto.builder().ofFieldRank(3).withRawValue(BRAND_NAME_RESOURCE_1.getRef()).build())
                .build();
        ContentEntryDto brandsEntry2 = ContentEntryDto.builder()
                .addItem(ContentItemDto.builder().ofFieldRank(1).withRawValue(BRAND_REF_2).build())
                .addItem(ContentItemDto.builder().ofFieldRank(2).withRawValue(BRAND_ID_RESOURCE_2.getRef()).build())
                .addItem(ContentItemDto.builder().ofFieldRank(3).withRawValue(BRAND_NAME_RESOURCE_2.getRef()).build())
                .build();
        DbDataDto brandsData = DbDataDto.builder()
                .forTopic(BRANDS)
                .addEntry(brandsEntry1)
                .addEntry(brandsEntry2)
                .build();
        DbResourceDto brandsResource = DbResourceDto.builder()
                .atVersion("1.0")
                .withCategoryCount(0)
                .containingEntries(asList(
                        ResourceEntryDto.builder()
                                .forReference(BRAND_ID_RESOURCE_1.getRef())
                                .withItems(singletonList(ResourceItemDto.builder()
                                        .withLocale(UNITED_STATES)
                                        .withValue(BRAND_ID_RESOURCE_1.getValue())
                                        .build()))
                                .build(),
                        ResourceEntryDto.builder()
                                .forReference(BRAND_ID_RESOURCE_2.getRef())
                                .withItems(singletonList(ResourceItemDto.builder()
                                        .withLocale(UNITED_STATES)
                                        .withValue(BRAND_ID_RESOURCE_2.getValue())
                                        .build()))
                                .build(),
                        ResourceEntryDto.builder()
                                .forReference(BRAND_NAME_RESOURCE_1.getRef())
                                .withItems(singletonList(ResourceItemDto.builder()
                                        .withLocale(UNITED_STATES)
                                        .withValue(BRAND_NAME_RESOURCE_1.getValue())
                                        .build()))
                                .build(),
                        ResourceEntryDto.builder()
                                .forReference(BRAND_NAME_RESOURCE_2.getRef())
                                .withItems(singletonList(ResourceItemDto.builder()
                                        .withLocale(UNITED_STATES)
                                        .withValue(BRAND_NAME_RESOURCE_2.getValue())
                                        .build()))
                                .build()))
                .build();
        DbDto brandsTopicObject = DbDto.builder()
                .withData(brandsData)
                .withResource(brandsResource)
                .build();

        when(miner.getContentEntryFromTopicWithReference(BRAND_REF_1, BRANDS)).thenReturn(of(brandsEntry1));
        when(miner.getContentEntryFromTopicWithReference(BRAND_REF_2, BRANDS)).thenReturn(of(brandsEntry2));
        when(miner.getContentEntryFromTopicWithReference(BRAND_NOREF, BRANDS)).thenReturn(empty());
        when(miner.getDatabaseTopic(BRANDS)).thenReturn(of(brandsTopicObject));
        when(miner.getLocalizedResourceValueFromTopicAndReference(BRAND_ID_RESOURCE_1.getRef(), BRANDS, UNITED_STATES)).thenReturn(of(BRAND_ID_RESOURCE_1.getValue()));
        when(miner.getLocalizedResourceValueFromTopicAndReference(BRAND_ID_RESOURCE_2.getRef(), BRANDS, UNITED_STATES)).thenReturn(of(BRAND_ID_RESOURCE_2.getValue()));
        when(miner.getLocalizedResourceValueFromTopicAndReference(BRAND_NAME_RESOURCE_1.getRef(), BRANDS, UNITED_STATES)).thenReturn(of(BRAND_NAME_RESOURCE_1.getValue()));
        when(miner.getLocalizedResourceValueFromTopicAndReference(BRAND_NAME_RESOURCE_2.getRef(), BRANDS, UNITED_STATES)).thenReturn(of(BRAND_NAME_RESOURCE_2.getValue()));
    }

    @Test
    public void getBrandFromReference_whenNonExistingRef_shouldReturnEmpty() throws Exception {
        // GIVEN-WHEN-THEN
        assertThat(brandHelper.getBrandFromReference(BRAND_NOREF)).isEmpty();
    }

    @Test
    public void getBrandFromReference_whenExistingRef_shouldReturnBrand() throws Exception {
        // GIVEN-WHEN
        Optional<Brand> actualBrand = brandHelper.getBrandFromReference(BRAND_REF_1);

        // THEN
        Brand expectedBrand = Brand.builder()
                .withReference(BRAND_REF_1)
                .withIdentifier(BRAND_ID_RESOURCE_1)
                .withDisplayedName(BRAND_NAME_RESOURCE_1)
                .build();
        assertThat(actualBrand).contains(expectedBrand);
    }

    @Test
    public void getBrandFromIdentifierOrName_whenNonExisting_shouldReturnEmpty() throws Exception {
        // GIVEN-WHEN-THEN
        assertThat(brandHelper.getBrandFromIdentifierOrName("azerty")).isEmpty();
    }

    @Test
    public void getBrandFromIdentifierOrName_whenExistingId_shouldReturnBrand() throws Exception {
        // GIVEN-WHEN
        Optional<Brand> actualBrand = brandHelper.getBrandFromIdentifierOrName("honda");

        // THEN
        Brand expectedBrand = Brand.builder()
                .withReference(BRAND_REF_2)
                .withIdentifier(BRAND_ID_RESOURCE_2)
                .withDisplayedName(BRAND_NAME_RESOURCE_2)
                .build();
        assertThat(actualBrand).contains(expectedBrand);
    }

    @Test
    public void getBrandFromIdentifierOrName_whenExistingName_shouldReturnBrand() throws Exception {
        // GIVEN-WHEN
        Optional<Brand> actualBrand = brandHelper.getBrandFromIdentifierOrName("alfa ROMEO");

        // THEN
        Brand expectedBrand = Brand.builder()
                .withReference(BRAND_REF_1)
                .withIdentifier(BRAND_ID_RESOURCE_1)
                .withDisplayedName(BRAND_NAME_RESOURCE_1)
                .build();
        assertThat(actualBrand).contains(expectedBrand);
    }

    @Test
    public void getAllBrands_shouldReturnAllEntries() throws Exception {
        // GIVEN-WHEN
        List<Brand> actualBrands = brandHelper.getAllBrands();

        // THEN
        assertThat(actualBrands).hasSize(2);
        assertThat(actualBrands).extracting("ref").containsOnly(BRAND_REF_1, BRAND_REF_2);
        assertThat(actualBrands).extracting("identifier").containsOnly(BRAND_ID_RESOURCE_1, BRAND_ID_RESOURCE_2);
        assertThat(actualBrands).extracting("displayedName").containsOnly(BRAND_NAME_RESOURCE_1, BRAND_NAME_RESOURCE_2);
    }
}
