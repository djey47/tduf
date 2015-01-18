package fr.tduf.libunlimited.low.files.research.common;

import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to handle formulas in file structure.
 */
public class FormulaHelper {

    private static final String FORMULA_PREFIX = "=" ;
    private static final Pattern POINTER_PATTERN = Pattern.compile("\\?(.+)\\?");     // e.g '?myValue?'

    /**
     * Evaluates given formula and returns result as integer.
     * @param formula   : formula to be evaluated
     * @param dataStore : current datastore, used to provide values (optional)
     * @return a computed result.
     */
    public static Integer resolveToInteger(String formula, DataStore dataStore) {
        if (formula == null) {
            return null;
        }

        if (formula.startsWith(FORMULA_PREFIX)) {
            formula = formula.substring(1);
        }

        formula = handlePatternWithStore(formula, dataStore);

        Expression expression = new ExpressionBuilder(formula).build();
        return ((Double) expression.evaluate()).intValue();
    }

    private static String handlePatternWithStore(String formula, DataStore dataStore) {
        Matcher matcher = POINTER_PATTERN.matcher(formula);

        if(!matcher.matches()) {
            return formula;
        }

        if (dataStore == null) {
            throw new IllegalArgumentException("A valid datastore is required to compute provided formula.");
        }

        String pointerReference = matcher.group(1);
        return dataStore.getInteger(pointerReference).get().toString();
    }
}