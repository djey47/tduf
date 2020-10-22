package fr.tduf.libunlimited.low.files.research.domain;

import fr.tduf.libunlimited.low.files.research.domain.fixture.DataStoreFixture;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinksContainerTest {

    private final LinksContainer linksContainer = new LinksContainer();

    @Test
    void registerSource_shouldAddLinkSource() {
        // given-when
        linksContainer.registerSource("a_field", 1024);

        // then
        assertThat(linksContainer.getSources().get(1024)).isEqualTo("a_field");
        assertThat(linksContainer.getTargets()).isEmpty();
    }

    @Test
    void register_whenAddressZero_shouldDoNothing() {
        // given-when
        linksContainer.register("a_field", 0, linksContainer.getSources());

        // then
        assertThat(linksContainer.getSources()).isEmpty();
    }

    @Test
    void register_whenAddressAlreadyRegistered_shouldThrowException() {
        // given
        linksContainer.registerSource("a_field", 1024);

        // when-then
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
                () -> linksContainer.register("another_field", 1024, linksContainer.getSources()));
        assertThat(actualException).hasMessage("A field key has already been registered as source @0x00000400 (1024)");
    }

    @Test
    void registerTarget_shouldAddLinkSource() {
        // given-when
        linksContainer.registerTarget("a_target_field", 1024);

        // then
        assertThat(linksContainer.getSources()).isEmpty();
        assertThat(linksContainer.getTargets().get(1024)).isEqualTo("a_target_field");
    }

    @Test
    void clear_shouldEmptyRegisters() {
        // given
        linksContainer.registerSource("a_field", 1024);
        linksContainer.registerTarget("another_field", 1024);

        // when
        linksContainer.clear();

        // then
        assertThat(linksContainer.getSources()).isEmpty();
        assertThat(linksContainer.getTargets()).isEmpty();
    }

    @Test
    void hasLinks_whenSource_shouldReturnTrue() {
        // given
        linksContainer.registerSource("a_field", 1024);

        // when-then
        assertThat(linksContainer.hasLinks()).isTrue();
    }

    @Test
    void hasLinks_whenTarget_shouldReturnTrue() {
        // given
        linksContainer.registerTarget("a_field", 1024);

        // when-then
        assertThat(linksContainer.hasLinks()).isTrue();
    }

    @Test
    void hasLinks_whenNoLink_shouldReturnfalse() {
        // given-when-then
        assertThat(linksContainer.hasLinks()).isFalse();
    }

    @Test
    void validate_whenNoLinks() {
        // given-when-then
        linksContainer.validate();
    }

    @Test
    void validate_whenValidLinks() {
        // given
        linksContainer.registerSource("a_field", 1024);
        linksContainer.registerTarget("another_field", 1024);

        // when-then
        linksContainer.validate();
    }

    @Test
    void validate_whenSourceAndTargetCountDifferent_shouldThrowException() {
        // given
        linksContainer.registerSource("a_field", 1024);

        // when-then
        IllegalStateException actualException = assertThrows(IllegalStateException.class,
                linksContainer::validate);
        assertThat(actualException).hasMessage("Mismatch in links container: 1 source(s) VS 0 target(s)");
    }

    @Test
    void validate_whenUnlinked_wsouldThrowException() {
        // given
        linksContainer.registerSource("a_field", 1024);
        linksContainer.registerTarget("another_field", 2048);

        // when-then
        IllegalStateException actualException = assertThrows(IllegalStateException.class,
                linksContainer::validate);
        assertThat(actualException).hasMessage("Issue in links container: 0 source(s) and target(s) linked, 1 remaining");
    }

    @Test
    void getSourcesSortedByAddress() {
        // given
        linksContainer.registerSource("a_field", 2048);
        linksContainer.registerSource("another_field", 1024);

        // when
        List<Map.Entry<Integer, String>> actualSources = linksContainer.getSourcesSortedByAddress();

        // then
        assertThat(actualSources).hasSize(2);
        assertThat(actualSources.stream().map(Map.Entry::getKey)).containsExactly(1024, 2048);
    }

    @Test
    void getTargetFieldKeyWithAddress_whenTargetLinkExists() {
        // given
        linksContainer.registerTarget("a_field", 1024);

        // when-then
        assertThat(linksContainer.getTargetFieldKeyWithAddress(1024)).contains("a_field");
    }

    @Test
    void getSourceFieldKeyWithAddress_whenTargetLinkExists() {
        // given
        linksContainer.registerSource("a_field", 1024);

        // when-then
        assertThat(linksContainer.getSourceFieldKeyWithAddress(1024)).contains("a_field");
    }

    @Test
    void getFieldKeyWithAddress_whenLinkDoesNotExist_shouldReturnEmpty() {
        // given-when-then
        assertThat(linksContainer.getFieldKeyWithAddress(1024, linksContainer.getSources())).isEmpty();
    }

    @Test
    void populateFromDatastore() throws IOException {
        // given
        DataStore dataStore = DataStoreFixture.createEmptyStore();
        DataStoreFixture.createStoreEntriesForLinkSources(dataStore);

        // when
        linksContainer.populateFromDatastore(dataStore);

        // then
        LinksContainer actualContainer = dataStore.getLinksContainer();
        assertThat(linksContainer.getSources()).isEqualTo(actualContainer.getSources());
        assertThat(linksContainer.getTargets()).isEqualTo(actualContainer.getTargets());
    }
}
