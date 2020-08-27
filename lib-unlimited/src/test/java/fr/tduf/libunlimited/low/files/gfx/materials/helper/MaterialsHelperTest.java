package fr.tduf.libunlimited.low.files.gfx.materials.helper;

import fr.tduf.libunlimited.high.files.db.miner.BulkDatabaseMiner;
import fr.tduf.libunlimited.low.files.db.dto.DbDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.DbResourceDto;
import fr.tduf.libunlimited.low.files.db.dto.resource.ResourceEntryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Map;
import java.util.Optional;

import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.CAR_COLORS;
import static fr.tduf.libunlimited.low.files.db.dto.DbDto.Topic.INTERIOR;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MaterialsHelperTest {

    @Mock
    private BulkDatabaseMiner minerMock;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void buildNormalizedDictionary() {
        // given
        DbResourceDto carColorsResources = DbResourceDto.builder()
                .atVersion("1.0")
                .containingEntries(singletonList(ResourceEntryDto.builder().forReference("REF1").withDefaultItem("Value___1").build()))
                .build();
        DbResourceDto interiorResources = DbResourceDto.builder()
                .atVersion("1.0")
                .containingEntries(asList(
                        ResourceEntryDto.builder().forReference("REF2").withDefaultItem("Value___2").build(),
                        ResourceEntryDto.builder().forReference("REF3").withDefaultItem("VALUE___2").build()))
                .build();
        when(minerMock.getResourcesFromTopic(CAR_COLORS)).thenReturn(of(carColorsResources));
        when(minerMock.getResourcesFromTopic(INTERIOR)).thenReturn(of(interiorResources));

        // when
        Map<String, String> actualDic = MaterialsHelper.buildNormalizedDictionary(minerMock);

        // then
        assertThat(actualDic).hasSize(2);
        assertThat(actualDic).containsEntry("\u0087ALUE___", "Value___1");
        assertThat(actualDic).containsEntry("\u0088ALUE___", "Value___2");
    }
}