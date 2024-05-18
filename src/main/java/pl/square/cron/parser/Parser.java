package pl.square.cron.parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

        // remove consecutive spaces
        cronExpression = cronExpression.replaceAll("\\s+", " ");
        // split to tokens
        String[] tokens = cronExpression.split(" ");
        if (tokens.length < 6) {
            throw new IllegalArgumentException("not enough fields in cron expression. Expected 6, provided: "+tokens.length);
        }

        String minuteDescription = parseMinute(tokens[0]);
        String hourDescription = parseHour(tokens[1]);
        String domDescription = parseDayOfMonth(tokens[2]);
        String monthDescription = parseMonth(tokens[3]);
        String dowDescription = parseDayOfWeek(tokens[4]);
        // remaining tokens joined
        List<String> tokensList = Arrays.asList(tokens);
        String command = String.join(" ", tokensList.subList(5, tokensList.size()));

        return List.of(minuteDescription,
                hourDescription,
                domDescription,
                monthDescription,
                dowDescription,
                command
                );
    }

    private String parseMonth(String token) {
        return parseField(token, 1, 12);
    }

    private String parseDayOfWeek(String token) {
        return parseField(token, 0, 7); // TODO replace 0 with 7?
    }

    private String parseDayOfMonth(String dayOfMonth) {
        return parseField(dayOfMonth, 1, 31); // TODO depend on month?
                                    // or values over real count will be silently ignored?
    }

    private String parseHour(String hourSymbol) {
        return parseField(hourSymbol, 0, 23);
    }

    private String parseMinute(String minuteSymbol) {
        return parseField(minuteSymbol, 0, 59);
    }

    // TODO refactor/optimize
    private String parseField(String symbol, int minValue, int maxValue) {
        // parse groups (commas)
        //   0-2,4,*/5 -> [0-2, 4, */5]
        List<String> minutes = Arrays.asList(symbol.split(","));
        // 1. expand star to range
        //      -> [0-2, 4, 0-59/5]
        minutes = minutes.stream().map(e -> e.replace("*", minValue+"-"+maxValue)).collect(Collectors.toList());
        // 2. expand groups to numbers
        //      -> [0,1,2, 4, 0,1,2,3,4,5,6,7,8,9,10...55,56,57,58,59/5]
        minutes = minutes.stream().map(e -> {
            String[] tokens = e.split("/");
            String numbers = tokens[0];
            String[] parts = numbers.split("-");
            if (parts.length > 1) {
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                String expanded = IntStream.rangeClosed(start, end).mapToObj(Integer::toString).collect(Collectors.joining(","));
                if (tokens.length > 1) { // with divisor
                    return expanded+"/"+tokens[1];
                }
                return expanded;
            } else {
                return e; // no hyphen -> change
            }
        }).collect(Collectors.toList());


        // 3. apply divisor
        //      -> [0,1,2, 4, 0,5,10...50,55]
        minutes = minutes.stream().map(e -> {
            if (e.contains("/")) {
                // everything before slash is a list of numbers
                String[] tokens = e.split("/");
                int divisor = Integer.parseInt(tokens[1]); // 5
                // temporary convert to ints
                List<String> numbers = Arrays.asList(tokens[0].split(",")); // 0,1,2,3,4,5 .. 55,56,57,58,59
                // and apply division
                List<String> afterDivision = numbers.stream().map(Integer::parseInt).filter(n -> n % divisor == 0)
                        .map(n -> Integer.toString(n)).collect(Collectors.toList());
                    // 0,5,10,15,..,50,55
                return String.join(",", afterDivision);
            } else {
                return e;
            }
                }).collect(Collectors.toList());

        // 4. flat the list, remove duplicates
        // -> [0, 1, 2, 4, 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55]
        minutes = minutes.stream().flatMap(e -> Stream.of(e.split(","))).distinct().collect(Collectors.toList());
        // 5. check min/max
        minutes.forEach(e -> {
            int n = Integer.parseInt(e); // throws NumberFormatException
            if (n < minValue) {
                throw new IllegalArgumentException("value "+n+" less than min "+minValue); // TODO add name
            }
            if (n > maxValue) {
                throw new IllegalArgumentException("value "+n+" greater than max "+maxValue); // TODO add name
            }
        });

        return String.join(" ", minutes);
    }



}
