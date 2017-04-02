package fr.tduf.libunlimited.common.helper;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AssertorHelperTest {
    @Test
    void assertSimpleCondition_when_ok_shouldDoNothing() throws Exception {
        // GIVEN-WHEN-THEN
        AssertorHelper.assertSimpleCondition(() -> 1 + 1 == 2);
    }

    @Test
    void assertSimpleCondition_when_ko_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> AssertorHelper.assertSimpleCondition(() -> 1 + 1 == 3));
    }

    @Test
    void assertSimpleCondition_withMessage_when_ko_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(IllegalStateException.class,
                () -> AssertorHelper.assertSimpleCondition(() -> 1 + 1 == 3, "^^ You dumbass!"));
    }
}
