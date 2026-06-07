package me.aanchev.belotej.engine;

import me.aanchev.belotej.domain.RelPlayer;
import me.aanchev.belotej.domain.Scores;
import me.aanchev.belotej.domain.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.provider.Arguments;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static me.aanchev.belotej.domain.RelPlayer.*;
import static me.aanchev.belotej.domain.Team.THEM;
import static me.aanchev.belotej.domain.Team.US;
import static me.aanchev.belotej.engine.RelStateUtils.rotate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class RotationTests {

    @TestFactory
    @DisplayName("Player Rotations")
    public Stream<DynamicContainer> _groupedByObserver_testPlayerRotation() {
        return groupByObserver(this::_source_testPlayerRotation, this::testPlayerRotation, this::displayName);
    }

    public void testPlayerRotation(RelPlayer observer, RelPlayer absolute, RelPlayer expected) {
        assertThat(rotate(absolute, observer.getIndex())).isEqualTo(expected);
    }

    private Stream<Arguments> _source_testPlayerRotation() {
        return Stream.of(
                // If I am South (s): everything remains as is
                Arguments.of(s, s, s),
                Arguments.of(s, w, w),
                Arguments.of(s, n, n),
                Arguments.of(s, e, e),

                // If I am West (w): everything is shifted anti-clockwise once
                Arguments.of(w, w, s),
                Arguments.of(w, n, w),
                Arguments.of(w, e, n),
                Arguments.of(w, s, e),

                // If I am North (n): everything is shifted anti-clockwise twice
                Arguments.of(n, n, s),
                Arguments.of(n, e, w),
                Arguments.of(n, s, n),
                Arguments.of(n, w, e),

                // If I am East (e): everything is shifted anti-clockwise three times
                Arguments.of(e, e, s),
                Arguments.of(e, s, w),
                Arguments.of(e, w, n),
                Arguments.of(e, n, e)
        );
    }


    @TestFactory
    @DisplayName("Team Rotations")
    public Stream<DynamicContainer> _groupedByObserver_testTeamRotation() {
        return groupByObserver(this::_source_testTeamRotation, this::testTeamRotation);
    }

    public void testTeamRotation(RelPlayer observer, Team absolute, Team expected) {
        assertThat(rotate(absolute, observer.getIndex())).isEqualTo(expected);
    }

    private Stream<Arguments> _source_testTeamRotation() {
        return Stream.of(
                // Observer South (s): US remains US
                Arguments.of(s, US, US),
                Arguments.of(s, THEM, THEM),

                // Observer West (w): THEM becomes US
                Arguments.of(w, THEM, US),
                Arguments.of(w, US, THEM),

                // Observer North (n): US remains US
                Arguments.of(n, US, US),
                Arguments.of(n, THEM, THEM),

                // Observer East (e): THEM becomes US
                Arguments.of(e, THEM, US),
                Arguments.of(e, US, THEM)
        );
    }


    @TestFactory
    @DisplayName("Score Rotations")
    public Stream<DynamicContainer> _groupedByObserver_testScoresRotation() {
        return groupByObserver(this::_source_testScoresRotation, this::testScoresRotation);
    }

    public void testScoresRotation(RelPlayer observer, Scores absolute, Scores expected) {
        assertThat(rotate(absolute, observer.getIndex())).isEqualTo(expected);
    }

    private Stream<Arguments> _source_testScoresRotation() {
        // Us: 12, Them: 34
        return Stream.of(
                // Observer South (s): 12, 34
                Arguments.of(s, new Scores(12, 34), new Scores(12, 34)),
                // Observer West (w): Us should be 34, Them should be 12
                Arguments.of(w, new Scores(12, 34), new Scores(34, 12)),
                // Observer North (n): 12, 34
                Arguments.of(n, new Scores(12, 34), new Scores(12, 34)),
                // Observer East (e): Us should be 34, Them should be 12
                Arguments.of(e, new Scores(12, 34), new Scores(34, 12))
        );
    }



    /* Rendering Helpers */

    private <G, I, E> Stream<DynamicContainer> groupByObserver(
            Supplier<Stream<Arguments>> sourceSupplier,
            TriConsumer<G, I, E> testMethod
    ) {
        return groupByObserver(sourceSupplier, testMethod, Object::toString);
    }

    @SuppressWarnings("unchecked")
    private <ANY, G extends ANY, I extends ANY, E extends ANY> Stream<DynamicContainer> groupByObserver(
            Supplier<Stream<Arguments>> sourceSupplier,
            TriConsumer<G, I, E> testMethod,
            Function<ANY, String> displayFormatter
    ) {
        return sourceSupplier.get().collect(groupingBy(arg -> (G) arg.get()[0]))
                .entrySet().stream()
                .map(entry -> {
                    var observer = entry.getKey();
                    var tests = entry.getValue().stream().map(arg -> {
                        var input = (I) arg.get()[1];
                        var expected = (E) arg.get()[2];
                        return (DynamicNode) dynamicTest(
                                String.format("Absolute %s should be %s", displayFormatter.apply(input), displayFormatter.apply(expected)),
                                () -> testMethod.accept(observer, input, expected)
                        );
                    });
                    return dynamicContainer("From the perspective of Absolute " + displayFormatter.apply(observer), tests);
                });
    }

    @FunctionalInterface
    private interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    private String displayName(RelPlayer player) {
        return switch (player) {
            case s -> "South";
            case w -> "West";
            case n -> "North";
            case e -> "East";
        };
    }
}
