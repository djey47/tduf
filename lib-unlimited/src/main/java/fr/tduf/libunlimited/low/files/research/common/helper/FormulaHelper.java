package fr.tduf.libunlimited.low.files.research.common.helper;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.NoSuchElementException;
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
     * @param formula       : formula to be evaluated
     * @param repeaterKey   : parent key to search value for, if necessary. May be null.
     * @param dataStore     : current datastore, used to provide values (optional)  @return a computed result.
     */
    // TODO repaterKey to Optional<String>
    public static Integer resolveToInteger(String formula, String repeaterKey, DataStore dataStore) {
        if (formula == null) {
            return null;
        }

        if (formula.startsWith(FORMULA_PREFIX)) {
            formula = formula.substring(1);
        }

        formula = handlePatternWithStore(formula, repeaterKey, dataStore);

        Expression expression = new ExpressionBuilder(formula).build();
        return ((Double) expression.evaluate()).intValue();
    }

    private static String handlePatternWithStore(String formula, String repeaterKeyPrefix, DataStore dataStore) {
        Matcher matcher = POINTER_PATTERN.matcher(formula);

        if(!matcher.matches()) {
            return formula;
        }

        if (dataStore == null) {
            throw new IllegalArgumentException("A valid datastore is required to compute provided formula.");
        }

        String pointerReference = matcher.group(1);

        String dataStoreValue = seekForLongValueInStore(pointerReference, repeaterKeyPrefix, dataStore);
        formula = formula.replace(String.format(POINTER_FORMAT, pointerReference), dataStoreValue);

        return formula;
    }

    private static String seekForLongValueInStore(String pointerReference, String repeaterKeyPrefix, DataStore dataStore) {
        Optional<Long> storedValue = Optional.empty();
        //1. Try to fetch in repeater if specified
        if (repeaterKeyPrefix != null) {
            storedValue = dataStore.getInteger(repeaterKeyPrefix + pointerReference);
        }
        //2. Try to fetch as such
        if (!storedValue.isPresent()) {
            storedValue = dataStore.getInteger(pointerReference);
        }

        try {
            return storedValue.get().toString();
        } catch (NoSuchElementException nse) {
            throw new IllegalArgumentException("Such an item does not exist in store: " + pointerReference, nse);
        }
    }
}