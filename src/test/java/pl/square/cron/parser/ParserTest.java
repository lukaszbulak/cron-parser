package pl.square.cron.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ParserTest {

    Parser parser = new Parser();

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "0",
            "0 0 0 0 ",
            "aa 99 c+d 0 0 cmd",
            "1- 1 0 0 0 cmd",
    })
    void failing(String cronExpression) {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> parser.parse(cronExpression));
    }

    @ParameterizedTest
    @MethodSource("provideCronStringsVsDescriptions")
    void passing(String cronExpression, List<String> expectedDescriptions) {
        List<String> descriptions = parser.parse(cronExpression);
        assertThat(descriptions).containsExactlyElementsOf(expectedDescriptions);
    }

 /*
       # run five minutes after midnight, every day
       5 0 * * *       $HOME/bin/daily.job >> $HOME/tmp/out 2>&1
       # run at 2:15pm on the first of every month -- output mailed to paul
       15 14 1 * *     $HOME/bin/monthly
       # run at 10 pm on weekdays, annoy Joe
       0 22 * * 1-5    mail -s "It's 10pm" joe%Joe,%%Where are your kids?%
       23 0-23/2 * * * echo "run 23 minutes after midn, 2am, 4am ..., everyday"
       5 4 * * sun     echo "run at 5 after 4 every sunday"
   */
    private static Stream<Arguments> provideCronStringsVsDescriptions() {
    return Stream.of(
      Arguments.of("0 0 1 1 0 cmd",                   List.of("0", "0", "1", "1", "0", "cmd"))
//      Arguments.of("*/15 0 1,15 * 1-5 /usr/bin/find", List.of("0 15 30 45", "0", "1 15", "1", "1 2 3 4 5", "cmd")),
//      Arguments.of("5 0 * * * $HOME/bin/daily.job",   List.of("5", "0", "1 2 3 4 5 6 7 8 9 10", "1", "0", "$HOME/bin/daily.job"))
    );
}
}
