package fr.tduf.libunlimited.common.helper;

import org.junit.Test;

public class AssertorHelperTest {
    @Test
    public void assertSimpleCondition_when_ok_shouldDoNothing() throws Exception {
        // GIVEN-WHEN-THEN
        AssertorHelper.assertSimpleCondition(() -> 1 + 1 == 2);
    }

    @Test(expected = IllegalStateException.class)
    public void assertSimpleCondition_when_ko_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        AssertorHelper.assertSimpleCondition(() -> 1 + 1 == 3);

        // THEN: ISE
    }

    @Test(expected = IllegalStateException.class)
    public void assertSimpleCondition_withMessage_when_ko_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        AssertorHelper.assertSimpleCondition(() -> 1 + 1 == 3, "^^ You dumbass!");

        // THEN: ISE
    }
}
