package ru.yandex.practicum.sleeptracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

public class SleeplessNightsFunction implements Function<List<SleepingSession>, SleepAnalysisResult> {

    @Override
    public SleepAnalysisResult apply(List<SleepingSession> sessions) {
        if (sessions.isEmpty()) {
            return new SleepAnalysisResult("Количество бессонных ночей", 0);
        }

        LocalDateTime firstStart = sessions.getFirst().getSleepStart();
        LocalDateTime lastEnd = sessions.getLast().getSleepEnd();

        // Если первая сессия начинается после полудня, первая потенциальная ночь – это следующее утро (дата+1)
        // Если первая сессия начинается до полудня, первая потенциальная ночь – это текущее утро (та же дата)
        LocalDate firstNightDate = firstStart.toLocalDate();
        if (firstStart.toLocalTime().isAfter(LocalTime.NOON)) {
            firstNightDate = firstNightDate.plusDays(1);
        }

        LocalDate lastNightDate = lastEnd.toLocalDate();

        long sleeplessCount = firstNightDate.datesUntil(lastNightDate.plusDays(1))
                .filter(date -> isNightSleepless(date, sessions))
                .count();

        return new SleepAnalysisResult("Количество бессонных ночей", sleeplessCount);
    }

    // Бессонная ночь наступает тогда, когда ни один сеанс не перекрывает интервал [00:00, 06:00].
    private boolean isNightSleepless(LocalDate date, List<SleepingSession> sessions) {
        LocalDateTime nightStart = date.atStartOfDay();
        LocalDateTime nightEnd = date.atTime(LocalTime.of(6, 0));
        return sessions.stream().noneMatch(s ->
                s.getSleepStart().isBefore(nightEnd) && s.getSleepEnd().isAfter(nightStart)
        );
    }
}
