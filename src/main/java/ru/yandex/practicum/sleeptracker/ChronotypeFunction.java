package ru.yandex.practicum.sleeptracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Function;

public class ChronotypeFunction implements Function<List<SleepingSession>, SleepAnalysisResult> {

    private static final LocalTime OWL_START_THRESHOLD  = LocalTime.of(23, 0);
    private static final LocalTime OWL_WAKE_THRESHOLD   = LocalTime.of(9, 0);
    private static final LocalTime LARK_START_THRESHOLD = LocalTime.of(22, 0);
    private static final LocalTime LARK_WAKE_THRESHOLD  = LocalTime.of(7, 0);

    @Override
    public SleepAnalysisResult apply(List<SleepingSession> sessions) {
        List<SleepingSession> nightSessions = sessions.stream()
                .filter(this::isNightSession)
                .toList();

        long owlCount  = nightSessions.stream().filter(s -> classifySession(s) == Chronotype.OWL).count();
        long larkCount = nightSessions.stream().filter(s -> classifySession(s) == Chronotype.LARK).count();
        long doveCount = nightSessions.stream().filter(s -> classifySession(s) == Chronotype.DOVE).count();

        return new SleepAnalysisResult("Хронотип пользователя", determineDominant(owlCount, larkCount, doveCount));
    }

    // Ночная сессия — та, что перекрывает интервал [00:00, 06:00] хотя бы для одной даты D
    private boolean isNightSession(SleepingSession s) {
        LocalDate startDate = s.getSleepStart().toLocalDate();
        LocalDate endDate = s.getSleepEnd().toLocalDate();
        return startDate.datesUntil(endDate.plusDays(1))
                .anyMatch(date -> {
                    LocalDateTime nightStart = date.atStartOfDay();
                    LocalDateTime nightEnd = date.atTime(LocalTime.of(6, 0));
                    return s.getSleepStart().isBefore(nightEnd) && s.getSleepEnd().isAfter(nightStart);
                });
    }

    private Chronotype classifySession(SleepingSession session) {
        LocalTime startTime = session.getSleepStart().toLocalTime();
        LocalTime endTime = session.getSleepEnd().toLocalTime();

        // Сова: засыпание после 23:00 И пробуждение после 9:00
        if (startTime.isAfter(OWL_START_THRESHOLD) && endTime.isAfter(OWL_WAKE_THRESHOLD)) {
            return Chronotype.OWL;
        }
        // Жаворонок: засыпание до 22:00 И пробуждение до 7:00
        if (startTime.isBefore(LARK_START_THRESHOLD) && endTime.isBefore(LARK_WAKE_THRESHOLD)) {
            return Chronotype.LARK;
        }
        // Все остальные случаи — Голубь
        return Chronotype.DOVE;
    }

    private Chronotype determineDominant(long owlCount, long larkCount, long doveCount) {
        long maxCount = Math.max(owlCount, Math.max(larkCount, doveCount));

        int typesAtMax = 0;
        if (owlCount  == maxCount) typesAtMax++;
        if (larkCount == maxCount) typesAtMax++;
        if (doveCount == maxCount) typesAtMax++;

        if (typesAtMax > 1) {
            return Chronotype.DOVE;
        }
        if (owlCount == maxCount)  return Chronotype.OWL;
        if (larkCount == maxCount) return Chronotype.LARK;
        return Chronotype.DOVE;
    }

}
