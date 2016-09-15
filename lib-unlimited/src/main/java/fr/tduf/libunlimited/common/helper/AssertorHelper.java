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
        if (condition.getAsBoolean()) {
            return;
        }

        throw new IllegalStateException("Unsatisfied condition. Please review code.");
    }
}
