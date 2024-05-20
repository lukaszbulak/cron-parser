package pl.square.cron.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ParserTest {

    Parser parser = new Parser();

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "0",
            "0 0 0 0 0"
    })
    void failingNotEnoughFields(String cronExpression) {
        assertThatThrownBy(() -> parser.parse(cronExpression))
                .isInstanceOfAny(IllegalArgumentException.class)
                .hasMessageContaining("not enough fields");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "aa 0 0 0 0 cmd",
            "0 c+d 0 0 0 cmd",
            "0 0 2.1 0 0 cmd",
            "1- 1 0 0 0 cmd", // this will not work as range
            "1/a 1 0 0 0 cmd", // this will not work as range
    })
    void failingInvalidNumbers(String cronExpression) {
        assertThatThrownBy(() -> parser.parse(cronExpression))
                .isInstanceOfAny(NumberFormatException.class)
                .hasMessageContaining("For input string");
    }

        @ParameterizedTest
    @ValueSource(strings = {
            "1/ 1 0 0 0 cmd"
    })
    void failingMissingDivisor(String cronExpression) {
        assertThatThrownBy(() -> parser.parse(cronExpression))
                .isInstanceOfAny(ArrayIndexOutOfBoundsException.class)
                .hasMessageContaining("out of bounds for length");
    }

          @ParameterizedTest
    @ValueSource(strings = {
            "60 1 1 1 0 cmd",
            "5 25 1 1 0 cmd",
            "5 5 32 1 0 cmd",
            "5 5 2 13 0 cmd",
            "5 5 2 1 9 cmd",
    })
    void failingOutsideMaxBounds(String cronExpression) {
        assertThatThrownBy(() -> parser.parse(cronExpression))
                .isInstanceOfAny(IllegalArgumentException.class)
                .hasMessageContaining("greater than max");
//        parser.parse(cronExpression);
    }



    @ParameterizedTest
    @MethodSource("provideCronStringsVsDescriptions")
    void passing(String cronExpression, List<String> expectedDescriptions) {
        List<String> descriptions = parser.parse(cronExpression);
        assertThat(descriptions).containsExactlyElementsOf(expectedDescriptions);
    }

    /**
     * sample from exercise:
     * *|15 0 1,15 * 1-5 /usr/bin/find                <-- pipe instead of slash so the comment won't be closed
     * <p>
     * samples from man 8 crontab:
     * # run five minutes after midnight, every day
     * 5 0 * * *       $HOME/bin/daily.job >> $HOME/tmp/out 2>&1
     * # run at 2:15pm on the first of every month -- output mailed to paul
     * 15 14 1 * *     $HOME/bin/monthly
     * # run at 10 pm on weekdays, annoy Joe
     * 0 22 * * 1-5    mail -s "It's 10pm" joe%Joe,%%Where are your kids?%
     * 23 0-23/2 * * * echo "run 23 minutes after midn, 2am, 4am ..., everyday"
     * 5 4 * * sun     echo "run at 5 after 4 every sunday"
     */
    private static Stream<Arguments> provideCronStringsVsDescriptions() {
        return Stream.of(
                Arguments.of("0 0 1 1 0 cmd",
                        List.of("0", "0", "1", "1", "7", "cmd")),
                Arguments.of("*/15 0 1,15 * 1-5 /usr/bin/find",
                        List.of("0 15 30 45", "0", "1 15", "1 2 3 4 5 6 7 8 9 10 11 12", "1 2 3 4 5", "/usr/bin/find")),
                Arguments.of("5 0 * * * $HOME/bin/daily.job >> $HOME/tmp/out 2>&1",
                        List.of("5", "0", "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31",
                                "1 2 3 4 5 6 7 8 9 10 11 12", "1 2 3 4 5 6 7", "$HOME/bin/daily.job >> $HOME/tmp/out 2>&1")),
                Arguments.of("15 14 1 * *     $HOME/bin/monthly",
                        List.of("15", "14", "1", "1 2 3 4 5 6 7 8 9 10 11 12", "1 2 3 4 5 6 7", "$HOME/bin/monthly")),
                Arguments.of("0 22 * * 1-5    mail -s \"It's 10pm\" joe%Joe,%%Where are your kids?%",
                        List.of("0", "22", "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31",
                                "1 2 3 4 5 6 7 8 9 10 11 12", "1 2 3 4 5", "mail -s \"It's 10pm\" joe%Joe,%%Where are your kids?%")),
                Arguments.of("23 0-23/2 * * * echo \"run 23 minutes after midn, 2am, 4am ..., everyday\"",
                        List.of("23", "0 2 4 6 8 10 12 14 16 18 20 22", "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31",
                                "1 2 3 4 5 6 7 8 9 10 11 12", "1 2 3 4 5 6 7", "echo \"run 23 minutes after midn, 2am, 4am ..., everyday\"")),
      Arguments.of("5 4 * * sun     echo \"run at 5 after 4 every sunday\"",
              List.of("5", "4", "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31",
                      "1 2 3 4 5 6 7 8 9 10 11 12", "7", "echo \"run at 5 after 4 every sunday\""))
        );
    }
}
