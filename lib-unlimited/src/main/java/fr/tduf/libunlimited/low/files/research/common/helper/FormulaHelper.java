package fr.tduf.libunlimited.low.files.research.common.helper;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to handle formulas in file structure.
 */
public class FormulaHelper {

    private static final String FORMULA_PREFIX = "=" ;
    private static final String POINTER_FORMAT = "?%s?";
    private static final Pattern POINTER_PATTERN = Pattern.compile(".*\\?(.+)\\?.*");     // e.g '?myValue?'

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

    private static String handlePatternWithStore(String formula, String potentialRepeaterKeyPrefix, DataStore dataStore) {
        Matcher matcher = POINTER_PATTERN.matcher(formula);

        if(!matcher.matches()) {
            return formula;
        }

        if (dataStore == null) {
            throw new IllegalArgumentException("A valid datastore is required to compute provided formula.");
        }

        String pointerReference = matcher.group(1);

        String dataStoreValue = seekForLongValueInStore(pointerReference, potentialRepeaterKeyPrefix, dataStore);
        formula = formula.replace(String.format(POINTER_FORMAT, pointerReference), dataStoreValue);

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
