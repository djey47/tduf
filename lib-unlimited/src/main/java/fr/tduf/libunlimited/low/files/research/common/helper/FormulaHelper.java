package fr.tduf.libunlimited.low.files.research.common.helper;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * Utility class to handle formulas in file structure.
 */
public class FormulaHelper {

    private static final String FORMULA_PREFIX = "=" ;
    private static final String POINTER_FORMAT = "?%s?";
    private static final Pattern POINTER_PATTERN = Pattern.compile("\\?(\\w+)\\?");     // e.g '?myValue?-?myOtherValue?'

    private static final Operator OPERATOR_GREATER_THAN = new Operator(">", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
        @Override
        public double apply(double[] values) {
            return values[0] > values[1] ? 1d : 0d;
        }
    };
    private static final Operator OPERATOR_LOWER_THAN = new Operator("<", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
        @Override
        public double apply(double[] values) {
            return values[0] < values[1] ? 1d : 0d;
        }
    };
    private static final Operator OPERATOR_EQUAL = new Operator("=", 2, true, Operator.PRECEDENCE_ADDITION - 1) {
        @Override
        public double apply(double[] values) {
            return values[0] == values[1] ? 1d : 0d;
        }
    };
    private static final List<Operator> CONDITIONAL_OPERATORS = asList(OPERATOR_EQUAL, OPERATOR_LOWER_THAN, OPERATOR_GREATER_THAN);

    /**
     * Evaluates given formula and returns result as integer.
     * @param formula               : formula to be evaluated
     * @param potentialRepeaterKey  : parent key to search value for, if necessary. May be null.
     * @param dataStore             : current datastore, used to provide values (optional)  
     * @return a computed result.
     */
    public static Integer resolveToInteger(String formula, String potentialRepeaterKey, DataStore dataStore) {
        if (formula == null) {
            return null;
        }

        if (formula.startsWith(FORMULA_PREFIX)) {
            formula = formula.substring(1);
        }

        formula = handlePatternWithStore(formula, potentialRepeaterKey, dataStore);

        Expression expression = new ExpressionBuilder(formula).build();
        return ((Double) expression.evaluate()).intValue();
    }

    /**
     * Evaluates given condition and returns result as integer.
     * Condition must remain simple with 2 operands, not including logical operators
     * Operators supported for now: &lt;, &gt;, =
     * @param condition             : formula to be evaluated
     * @param potentialRepeaterKey  : parent key to search value for, if necessary. May be null.
     * @param dataStore             : current datastore, used to provide values (optional)
     * @return a computed result.
     */
    public static boolean resolveCondition(String condition, String potentialRepeaterKey, DataStore dataStore) {
        requireNonNull(condition, "Condition to evaluate is required");

        String resolvedCondition = handlePatternWithStore(condition, potentialRepeaterKey, dataStore);

        Expression expression = new ExpressionBuilder(resolvedCondition)
                .operator(CONDITIONAL_OPERATORS)
                .build();
        return expression.evaluate() == 1d;
    }

    private static String handlePatternWithStore(String formula, String potentialRepeaterKeyPrefix, DataStore dataStore) {
        Matcher matcher = POINTER_PATTERN.matcher(formula);
        boolean hasMatch = matcher.find();
        if (!hasMatch) {
            return formula;
        }

        if (dataStore == null) {
            throw new IllegalArgumentException("A valid datastore is required to compute provided formula.");
        }

        while (hasMatch) {
            String pointerReference = matcher.group(1);
            String dataStoreValue = seekForLongValueInStore(pointerReference, potentialRepeaterKeyPrefix, dataStore);
            formula = formula.replace(String.format(POINTER_FORMAT, pointerReference), dataStoreValue);
            hasMatch = matcher.find();
        }

        return formula;
    }

    private static String seekForLongValueInStore(String pointerReference, String potentialRepeaterKeyPrefix, DataStore dataStore) {
        Optional<Long> storedValue = Optional.empty();
        //1. Try to fetch in repeater if specified
        if (potentialRepeaterKeyPrefix != null) {
            storedValue = dataStore.getInteger(potentialRepeaterKeyPrefix + pointerReference);
        }
        //2. Try to fetch as such
        if (!storedValue.isPresent()) {
            storedValue = dataStore.getInteger(pointerReference);
        }

        return storedValue
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("Such an item does not exist in store: " + pointerReference));
    }
}
