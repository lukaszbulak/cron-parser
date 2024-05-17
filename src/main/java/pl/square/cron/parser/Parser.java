package pl.square.cron.parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
             field          allowed values
             -----          --------------
             minute         0-59
             hour           0-23
             day of month   1-31
             month          1-12 (or names, see below)
             day of week    0-7 (0 or 7 is Sunday, or use names)
 */
public class Parser {

    /** returns full description for each of elements */
    public List<String> parse(String cronExpression) {

        String[] tokens = cronExpression.split(" ");
        if (tokens.length < 6) {
            throw new IllegalArgumentException("not enough fields in cron expression. Expected 6, provided: "+tokens.length);
        }

        String minuteDescription = parseMinute(tokens[0], 0, 59);
        String hourDescription = parseHour(tokens[1]);
        String domDescription = parseDayOfMonth(tokens[2]);
        String monthDescription = parseMonth(tokens[3]);
        String dowDescription = parseDayOfWeek(tokens[4]);
        String command = tokens[5];

        return List.of(minuteDescription,
                hourDescription,
                domDescription,
                monthDescription,
                dowDescription,
                command
                );
    }

    private static String parseMonth(String token) {
        return token;
    }

    private static String parseDayOfWeek(String token) {
        return token;
    }

    private static String parseDayOfMonth(String domToken) {
        return domToken;
    }

    private static String parseHour(String hourSymbol) {
        return hourSymbol;
    }

    // Work-In-Progress ...
    private static String parseMinute(String minuteSymbol, int minValue, int maxValue) {
        // parse groups (commas)
        List<String> minutes = Arrays.asList(minuteSymbol.split(","));
        // 1 expand star to range
        minutes = minutes.stream().map(e -> e.replace("*", "0-"+maxValue)).collect(Collectors.toList());
        // expand groups to numbers (with dividers)
/*        minutes = minutes.stream().map(e -> {
            String[] parts = e.split("-");

        }).collect(Collectors.toList());
 */
        // verify numbers, check max
        minutes.forEach(e -> {
            int n = Integer.parseInt(e); // throws NumberFormatException
            if (n < minValue) {
                throw new IllegalArgumentException("value less than "+minValue); // TODO add name
            }
            if (n > maxValue) {
                throw new IllegalArgumentException("value greater than "+maxValue); // TODO add name
            }

        });

        // parse range
//        minutes.stream().map()

        return String.join(",", minutes);
    }



}
