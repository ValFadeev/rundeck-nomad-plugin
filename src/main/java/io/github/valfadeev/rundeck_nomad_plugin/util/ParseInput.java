package io.github.valfadeev.rundeck_nomad_plugin.util;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ParseInput {
    public static String[] checkedSplit(String input, String delimiter) {
        String[] result = input.split(delimiter);
        if (result.length > 1) {
            return result;
        } else {
            throw new IllegalArgumentException(
                    String.format("valid input must be a string delimited by \"%s\", got: \"%s\"",
                            delimiter, input));
        }

    }
    public static Map<String, String> kvToMap(String input) {
        return Arrays.stream(input.split("\n"))
                .map(s -> checkedSplit(s, "="))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
    }
}
