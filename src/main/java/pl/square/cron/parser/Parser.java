package pl.square.cron.parser;

import java.text.DateFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;

/**
 * This class does actual cron pattern parsing.
 * The input consist of string with 5 parts:
 * <code>
 * field          allowed values
 * -----          --------------
 * minute         0-59
 * hour           0-23
 * day of month   1-31
 * month          1-12 (or names, see below)
 * day of week    0-7 (0 or 7 is Sunday, or use names)
 * </code>
 * Output is a list of strings with all ranges and divisors expanded - providing all possible values to consider.
 *
 */
public class Parser {

    /**
     * Returns full description for each of elements
     */
    public List<String> parse(String cronExpression) {

        // remove consecutive spaces
        cronExpression = cronExpression.replaceAll("\\s+", " ");
        // split to tokens
        String[] tokens = cronExpression.split(" ");
        if (tokens.length < 6) {
            throw new IllegalArgumentException("not enough fields in cron expression. Expected 6, provided: " + tokens.length);
        }

        String minuteDescription = parseMinute(tokens[0]);
        String hourDescription = parseHour(tokens[1]);
        String domDescription = parseDayOfMonth(tokens[2]);
        String monthDescription = parseMonth(tokens[3]);
        String dowDescription = parseDayOfWeek(tokens[4]);
        // remaining tokens joined: 6th (0-indexed 5) and any following
        String[] cmdTokens = copyOfRange(tokens, 5, tokens.length);
        String command = String.join(" ", asList(cmdTokens));

        return List.of(minuteDescription,
                hourDescription,
                domDescription,
                monthDescription,
                dowDescription,
                command
        );
    }

    private String parseMonth(String token) {
        // replace short month name with 1-base number
        String[] months = new DateFormatSymbols(Locale.US).getShortMonths();
        for (int i = 0; i < months.length-1; i++) {
            token = token.replace(months[i], String.valueOf(i + 1));
        }
        return parseField(token, 1, 12, "month");
    }

    private String parseDayOfWeek(String token) {
        // replace short day name with 1-base number
        token = token.toLowerCase();
        // This locale could be used, but is starting from sunday. Easier to provide own list.
//        String[] weekdays = new DateFormatSymbols(Locale.US).getShortWeekdays();
        String[] weekdays = new String[]{"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
        for (int i = 0; i < weekdays.length; i++) {
            token = token.replace(weekdays[i], String.valueOf(i+1));
        }
        token = token.replace("0", "7");
        return parseField(token, 1, 7, "day of week");
    }

    private String parseDayOfMonth(String dayOfMonth) {
        // does max value depend on month?
        // or values over real days count in current month will be silently ignored?
        return parseField(dayOfMonth, 1, 31, "day of month");
    }

    private String parseHour(String hourSymbol) {
        return parseField(hourSymbol, 0, 23, "hour");
    }

    private String parseMinute(String minuteSymbol) {
        return parseField(minuteSymbol, 0, 59, "minute");
    }

    private String parseField(String symbol, int minValue, int maxValue, String fieldName) {
        try {
            // parse groups (commas)
            //   0-2,4,*/5 -> [0-2, 4, */5]
            List<String> provided = asList(symbol.split(","));
            List<String> expanded = provided.stream()
                    // 1. expand star to range
                    //      -> [0-2, 4, 0-59/5]
                    .map(e -> e.replace("*", minValue + "-" + maxValue))
                    // 2. expand groups to numbers
                    //      -> [0,1,2, 4, 0,1,2,3,4,5,6,7,8,9,10...55,56,57,58,59/5]
                    .map(Parser::expandRanges)
                    // 3. apply divisor
                    //      -> [0,1,2, 4, 0,5,10...50,55]
                    .map(Parser::applyDivisor)
                    // 4. flat the list, remove duplicates
                    // -> [0, 1, 2, 4, 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55]
                    .flatMap(e -> Stream.of(e.split(","))).distinct().toList();

            // 5. check min/max
            expanded.forEach(e -> {
                int n = Integer.parseInt(e); // throws NumberFormatException
                if (n < minValue) {
                    throw new IllegalArgumentException(fieldName + " value " + n + " less than min " + minValue);
                }
                if (n > maxValue) {
                    throw new IllegalArgumentException(fieldName + " value " + n + " greater than max " + maxValue);
                }
            });
            return String.join(" ", expanded);
        } catch (Exception e) {
            // following print could be other way...
            System.out.println("cannot parse " + fieldName + ": "+e.getMessage());
            throw e;
        }
    }

    /**
     * 3. apply divisor
     * -> [0,1,2, 4, 0,5,10...50,55]
     */
    private static String applyDivisor(String e) {
        if (e.contains("/")) {
            // everything before slash is a list of numbers
            String[] tokens = e.split("/");
            int divisor = Integer.parseInt(tokens[1]); // 5
            // temporary convert to ints
            List<String> numbers = asList(tokens[0].split(",")); // 0,1,2,3,4,5 .. 55,56,57,58,59
            // and apply division
            List<String> afterDivision = numbers.stream().map(Integer::parseInt).filter(n -> n % divisor == 0)
                    .map(n -> Integer.toString(n)).collect(Collectors.toList());
            // 0,5,10,15,..,50,55
            return String.join(",", afterDivision);
        } else {
            return e;
        }
    }

    /**
     * 2. expand groups to numbers
     * //      -> [0,1,2, 4, 0,1,2,3,4,5,6,7,8,9,10...55,56,57,58,59/5]
     */
    private static String expandRanges(String e) {
        // preserve divisor
        String[] tokens = e.split("/");
        String numbers = tokens[0];
        String[] parts = numbers.split("-");
        if (parts.length > 1) {
            int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);
            String expanded = IntStream.rangeClosed(start, end).mapToObj(Integer::toString).collect(Collectors.joining(","));
            if (tokens.length > 1) { // with divisor
                return expanded + "/" + tokens[1];
            }
            return expanded;
        } else {
            return e;
        }
    }


}
