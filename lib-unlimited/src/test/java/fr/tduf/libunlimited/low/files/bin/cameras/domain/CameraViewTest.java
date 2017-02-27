package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Bumper;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.BINOCULARS;
import static org.assertj.core.api.Assertions.assertThat;

class CameraViewTest {
    @Test
    void cloneForNewViewSet_shouldUpdateSetIdentifierAndCreateOwnReferences() {
        // given
        EnumMap<ViewProps, Object> settings = new EnumMap<>(ViewProps.class);
        settings.put(BINOCULARS, 10L);
        DataStore originalDataStore = new DataStore(FileStructureDto.builder().build());
        CameraView source = CameraView.builder()
                .forCameraSetId(1)
                .fromDatastore(originalDataStore)
                .withSettings(settings)
                .ofKind(Bumper)
                .withName("name")
                .withLabel("label")
                .build();

        // when
        CameraView actualClone = source.cloneForNewViewSet(100);

        // then
        assertThat(actualClone).isNotSameAs(source);
        assertThat(actualClone.getCameraSetId()).isEqualTo(100);
        assertThat(actualClone.getKind()).isEqualTo(Bumper);
        assertThat(actualClone.getName()).isEqualTo("name");
        assertThat(actualClone.getLabel()).isEqualTo("label");
        assertThat(actualClone.getOriginalDataStore()).isNotSameAs(originalDataStore);
        assertThat(actualClone.getSettings())
                .isNotSameAs(settings)
                .containsOnlyKeys(BINOCULARS)
                .containsValues(10L);
    }
}
