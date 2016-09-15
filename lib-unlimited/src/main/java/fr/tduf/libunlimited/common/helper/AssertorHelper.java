package fr.tduf.libunlimited.common.helper;

import java.util.function.BooleanSupplier;

/**
 * Hosts methods to replace good old assert keywork, throwing appropriate runtime exception
 */
public class AssertorHelper {
    private AssertorHelper() {}

    /**
     * @param condition : simple condition to evaluate
     * @throws IllegalStateException if above condition is not satisfied.
     */
    public static void assertSimpleCondition(BooleanSupplier condition) {
        assertSimpleCondition(condition, "Unsatisfied condition. Please review code.");
    }

    /**
     * @param condition : simple condition to evaluate
     * @param message   : message to be brought by the exception
     * @throws IllegalStateException if above condition is not satisfied.
     */
    public static void assertSimpleCondition(BooleanSupplier condition, String message) {
        if (condition.getAsBoolean()) {
            return;
        }

        throw new IllegalStateException(message);
    }
}
