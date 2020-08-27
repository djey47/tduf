package fr.tduf.libunlimited.common.game.helper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class GameEngineHelperTest {

    @Test
    void normalizeString_whenNullString_shouldReturnNull() {
        // given-when-then
        assertThat(GameEngineHelper.normalizeString(null)).isNull();
    }

    @Test
    void normalizeString_when8CharString_shouldReturnItAsIs() {
        // given-when-then
        assertThat(GameEngineHelper.normalizeString("ABCDEFGH")).isEqualTo("ABCDEFGH");
    }

    @Test
    void normalizeString_when9CharString_shouldNormalizeIt() {
        // given-when-then
        assertThat(GameEngineHelper.normalizeString("FORDGT_02")).isEqualTo("xORDGT_0");
    }

    @Test
    void normalizeString_when11CharString_shouldNormalizeIt() {
        // given-when-then
        assertThat(GameEngineHelper.normalizeString("FORDGT_03_N")).isEqualTo("y®\u00A0DGT_0");
    }

    @Test
    void normalizeString_when24CharString_shouldNormalizeIt() {
        // given-when-then
        assertThat(GameEngineHelper.normalizeString("FORDGT_03_NOPQRSTUVWXYZ0")).isEqualTo("Íăöêïþċ");
    }
}