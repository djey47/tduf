package fr.tduf.cli.common.helper;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Helper class to handle tool commands.
 */
public class CommandHelper {

    /**
     * Returns all available labels.
     * @param commandEnum   : Any enum value.
     * @return a set of all labels.
     */
    public static Set<String> getLabels(CommandEnum commandEnum) {
        return Stream.of(commandEnum.getValues())

                .map(CommandEnum::getLabel)

                .collect(toSet());
    }

    /**
     * Returns all enum values with key-value pairs (label, description)
     * @param commandEnum   : Any enum value.
     * @return a map of all values.
     */
    public static Map<String, String> getValuesAsMap(CommandEnum commandEnum) {
        return Stream.of(commandEnum.getValues())

                .collect(Collectors.toMap(CommandEnum::getLabel, CommandEnum::getDescription));
    }

    /**
     * Returns enum value corresponding to given label.
     * @param commandEnum   : Any enum value.
     * @param label         : Label to search value for.
     * @return a value.
     */
    public static CommandEnum fromLabel(CommandEnum commandEnum, String label) {
        return Stream.of(commandEnum.getValues())
                .filter(cmd -> cmd.getLabel().equals(label))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Provided command has no member with provided label: " + label));
    }

    /**
     * Contract to implement for all app commands.
     */
    public interface CommandEnum {

        /**
         * @return value of label field.
         */
        String getLabel();

        /**
         * @return value of description field.
         */
        String getDescription();

        /**
         * @return all enum values.
         */
        CommandEnum[] getValues();
    }
}