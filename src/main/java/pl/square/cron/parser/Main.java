package pl.square.cron.parser;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        // TODO name/header

        if (args.length < 1) {
            System.out.println("please provide cron expression in parenthesis");
            return;
            // TODO handle without parenthesis - as 6 parameters
        }

        List<String> descriptions = new Parser().parse(args[0]);  // "*/15 0 1,15 * 1-5 /usr/bin/find"

        // ---- presentation:
        System.out.printf("%-14s %s\n", "minute",       descriptions.get(0));
        System.out.printf("%-14s %s\n", "hour",         descriptions.get(1));
        System.out.printf("%-14s %s\n", "day of month", descriptions.get(2));
        System.out.printf("%-14s %s\n", "month",        descriptions.get(3));
        System.out.printf("%-14s %s\n", "day of week",  descriptions.get(4));
        System.out.printf("%-14s %s\n", "command",      descriptions.get(5));
    }


}
