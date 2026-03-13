package ru.yandex.practicum.sleeptracker;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MaxDurationFunction implements Function<List<SleepingSession>, SleepAnalysisResult> {

    @Override
    public SleepAnalysisResult apply(List<SleepingSession> sessions) {
        long max = sessions.stream()
                .filter(Objects::nonNull)
                .mapToLong(SleepingSession::getDurationMinutes)
                .max()
                .orElse(0);
        return new SleepAnalysisResult("Максимальная продолжительность сессии (мин)", max);
    }
}
