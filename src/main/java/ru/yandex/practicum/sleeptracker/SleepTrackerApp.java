package ru.yandex.practicum.sleeptracker;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class SleepTrackerApp {

    private static final String DEFAULT_LOG = "src/main/resources/sleep_log.txt";

    private static final List<Function<List<SleepingSession>, SleepAnalysisResult>> ANALYZERS = List.of(
            new TotalSessionsFunction(),
            new MinDurationFunction(),
            new MaxDurationFunction(),
            new AvgDurationFunction(),
            new BadQualityCountFunction(),
            new SleeplessNightsFunction(),
            new ChronotypeFunction()
    );

    public static void main(String[] args) {
        List<SleepingSession> sessions = SleepLogReader.read(Path.of(DEFAULT_LOG));

        ANALYZERS.stream()
                .map(f -> f.apply(sessions))
                .forEach(System.out::println);
    }
}
