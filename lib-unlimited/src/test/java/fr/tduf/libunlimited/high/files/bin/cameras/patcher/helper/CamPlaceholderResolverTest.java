package fr.tduf.libunlimited.high.files.bin.cameras.patcher.helper;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.ViewChangeDto;
import fr.tduf.libunlimited.high.files.common.patcher.domain.PatchProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewKind.Follow_Large_Back;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.BINOCULARS;
import static fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps.VIEW_POSITION_X;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class CamPlaceholderResolverTest {

    @Mock
    private CamPatchDto patchObjectMock;

    @Mock
    private PatchProperties patchPropertiesMock;

    @InjectMocks
    private CamPlaceholderResolver camPlaceholderResolver;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void resolveAllPlaceholders_whenPlaceholders_andProperties_shouldResolveThem() {
        // given
        ViewChangeDto viewChangeDto = ViewChangeDto.builder()
                .forViewKind(Follow_Large_Back)
                .addProp(BINOCULARS, "50")
                .addPropForPlaceholder(VIEW_POSITION_X, "V.POS.X")
                .build();
        SetChangeDto setChangeDto = SetChangeDto.builder()
                .withPlaceholderForSetIdentifier("SET.ID")
                .addChanges(singletonList(viewChangeDto))
                .build();
        when(patchObjectMock.getChanges()).thenReturn(singletonList(setChangeDto));
        when(patchPropertiesMock.retrieve("SET.ID")).thenReturn(of("125"));
        when(patchPropertiesMock.retrieve("V.POS.X")).thenReturn(of("10000"));

        // when
        camPlaceholderResolver.resolveAllPlaceholders();

        // then
        assertThat(setChangeDto.getId()).isEqualTo("125");
        assertThat(setChangeDto.getChanges().get(0).getViewProps().get(BINOCULARS)).isEqualTo("50");
        assertThat(setChangeDto.getChanges().get(0).getViewProps().get(VIEW_POSITION_X)).isEqualTo("10000");
    }
}
