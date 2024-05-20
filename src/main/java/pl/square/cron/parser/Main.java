package pl.square.cron.parser;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("Cron parser. Provide cron pattern as argument to get description of expanded fields.");
        System.out.println();

        String cronPattern;
        if (args.length == 1) {
            // "*/15 0 1,15 * 1-5 /usr/bin/find"
            cronPattern = args[0];
        } else if (args.length >= 6) {
            // handle without parenthesis - as 6+ parameters
            cronPattern = String.join(" ", Arrays.asList(args));
        } else {
            System.out.println("please provide cron expression in parenthesis: ");
            System.out.println("   \"*/15 0 1,15 * 1-5 /usr/bin/find\"");
            return;
        }

        System.out.println("Description of pattern: "+cronPattern);
        List<String> descriptions = new Parser().parse(cronPattern);

        // ---- presentation:
        System.out.printf("%-14s %s\n", "minute",       descriptions.get(0));
        System.out.printf("%-14s %s\n", "hour",         descriptions.get(1));
        System.out.printf("%-14s %s\n", "day of month", descriptions.get(2));
        System.out.printf("%-14s %s\n", "month",        descriptions.get(3));
        System.out.printf("%-14s %s\n", "day of week",  descriptions.get(4));
        System.out.printf("%-14s %s\n", "command",      descriptions.get(5));
    }


}
