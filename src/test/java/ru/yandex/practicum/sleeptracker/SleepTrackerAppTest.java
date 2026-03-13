package ru.yandex.practicum.sleeptracker;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SleepTrackerAppTest {

    // Вспомогательные функции

    /** Создает сессию, которая потенциально может охватывать период в один месяц */
    private static SleepingSession session(int year, int month, int startDay, int startH, int startM,
                                     int endYear, int endMonth, int endDay, int endH, int endM,
                                     SleepQuality quality) {
        return new SleepingSession(
                LocalDateTime.of(year, month, startDay, startH, startM),
                LocalDateTime.of(endYear, endMonth, endDay, endH, endM),
                quality
        );
    }

    /** Сокращенный вариант для сессий в течение одного года/месяца (время между двумя днями может пересекаться с полуночью). */
    private static SleepingSession night(int year, int month,
                                         int startDay, int startH, int startM,
                                         int endDay, int endH, int endM,
                                         SleepQuality quality) {
        return session(year, month, startDay, startH, startM, year, month, endDay, endH, endM, quality);
    }

    // Функции подсчета сессий

    @Test
    void totalSessions_emptyList_returnsZero() {
        SleepAnalysisResult result = new TotalSessionsFunction().apply(List.of());
        assertEquals(0, result.getValue());
    }

    @Test
    void totalSessions_threeSessions_returnsThree() {
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD),
                night(2025, 10, 2, 23, 0, 3, 7, 0, SleepQuality.NORMAL),
                night(2025, 10, 3, 14, 0, 3, 15, 0, SleepQuality.NORMAL)
        );
        SleepAnalysisResult result = new TotalSessionsFunction().apply(sessions);
        assertEquals(3, result.getValue());
    }

    // Функции подсчета минимальной продолжительности

    @Test
    void minDuration_singleSession_returnsDuration() {
        // от 23:00 до 07:00 = 480 минут
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new MinDurationFunction().apply(sessions);
        assertEquals(480L, result.getValue());
    }

    @Test
    void minDuration_multipleSessions_returnsSmallest() {
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD),   // 480 min
                night(2025, 10, 2, 14, 0, 2, 15, 0, SleepQuality.NORMAL), //  60 min
                night(2025, 10, 3, 23, 0, 4, 8, 0, SleepQuality.BAD)     // 540 min
        );
        SleepAnalysisResult result = new MinDurationFunction().apply(sessions);
        assertEquals(60L, result.getValue());
    }

    // Функции подсчета максимальной продолжительности

    @Test
    void maxDuration_singleSession_returnsDuration() {
        // от 23:00 до 07:00 = 480 минут
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new MaxDurationFunction().apply(sessions);
        assertEquals(480L, result.getValue());
    }

    @Test
    void maxDuration_multipleSessions_returnsLargest() {
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD),   // 480 min
                night(2025, 10, 2, 14, 0, 2, 15, 0, SleepQuality.NORMAL), //  60 min
                night(2025, 10, 3, 23, 0, 4, 8, 0, SleepQuality.BAD)     // 540 min
        );
        SleepAnalysisResult result = new MaxDurationFunction().apply(sessions);
        assertEquals(540L, result.getValue());
    }

    // Функции средней продолжительности

    @Test
    void avgDuration_emptyList_returnsZero() {
        SleepAnalysisResult result = new AvgDurationFunction().apply(List.of());
        assertEquals(0L, result.getValue());
    }

    @Test
    void avgDuration_twoSessions_returnsAverage() {
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD),   // 480 min
                night(2025, 10, 2, 14, 0, 2, 16, 0, SleepQuality.NORMAL)  // 120 min
        );
        // avg = (480 + 120) / 2 = 300
        SleepAnalysisResult result = new AvgDurationFunction().apply(sessions);
        assertEquals(300L, result.getValue());
    }

    // Функции подсчета количества сессий с плохим сном

    @Test
    void badQualityCount_noBadSessions_returnsZero() {
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD),
                night(2025, 10, 2, 23, 0, 3, 7, 0, SleepQuality.NORMAL)
        );
        SleepAnalysisResult result = new BadQualityCountFunction().apply(sessions);
        assertEquals(0L, result.getValue());
    }

    @Test
    void badQualityCount_mixedQuality_returnsCorrectCount() {
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD),
                night(2025, 10, 2, 23, 0, 3, 7, 0, SleepQuality.BAD),
                night(2025, 10, 3, 14, 0, 3, 15, 0, SleepQuality.NORMAL),
                night(2025, 10, 3, 23, 0, 4, 7, 0, SleepQuality.BAD)
        );
        SleepAnalysisResult result = new BadQualityCountFunction().apply(sessions);
        assertEquals(2L, result.getValue());
    }

    // Функции бессонных ночей

    @Test
    void sleeplessNights_emptyList_returnsZero() {
        SleepAnalysisResult result = new SleeplessNightsFunction().apply(List.of());
        assertEquals(0, result.getValue());
    }

    @Test
    void sleeplessNights_allNightsCovered_returnsZero() {
        // 1 окт. 23:00 – 2 окт. 07:00 → ночь 2 окт.
        // 2 окт. 23:00 – 3 окт. 07:00 → ночь 3 окт.
        // Обе ночи не бессонные
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD),
                night(2025, 10, 2, 23, 0, 3, 7, 0, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new SleeplessNightsFunction().apply(sessions);
        assertEquals(0L, result.getValue());
    }

    @Test
    void sleeplessNights_oneGapBetweenSessions_returnsOne() {
        // 1 окт. 23:00 – 2 окт. 07:00 → ночь 2 окт.
        // 3 окт. 23:00 – 4 окт. 07:00 → ночь 4 окт.
        // 3 окт. Ночь без сессии сна → 1 бессонная ночь
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD),
                night(2025, 10, 3, 23, 0, 4, 7, 0, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new SleeplessNightsFunction().apply(sessions);
        assertEquals(1L, result.getValue());
    }

    @Test
    void sleeplessNights_firstSessionBeforeNoon_firstNightIsIncluded() {
        // Сеанс начинается в 01:00 → firstNightDate = 5 окт.
        // Сеанс с 01:00 до 05:30 перекрывается [5 окт. 00:00, 5 окт. 06:00] → ночь учитывается как не бессонная
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 5, 1, 0, 5, 5, 30, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new SleeplessNightsFunction().apply(sessions);
        assertEquals(0L, result.getValue());
    }

    @Test
    void sleeplessNights_firstSessionBeforeNoonAfterSixAm_firstNightSleepless() {
        // Сеанс начинается в 08:00 (до полудня) → firstNightDate = 5 окт.
        // Сеанс начинается ПОСЛЕ 06:00, НЕ охватывает период [5 окт. 00:00, 5 окт. 06:00] → считается как бессонница
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 5, 8, 0, 5, 10, 0, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new SleeplessNightsFunction().apply(sessions);
        assertEquals(1L, result.getValue());
    }

    @Test
    void sleeplessNights_crossMonthBoundaryNoCoverage_countedCorrectly() {
        // 31 окт. 23:00 – 1 нояб. 07:00 → охватывает ночь 1 нояб.
        // 1 нояб. 23:00 – 2 нояб. 07:00 → охватывает ночь 2 нояб.
        // firstNightDate = 1 нояб., lastNightDate = 2 нояб. → 0 бессонных ночей
        List<SleepingSession> sessions = List.of(
                session(2025, 10, 31, 23, 0, 2025, 11, 1, 7, 0, SleepQuality.GOOD),
                session(2025, 11, 1, 23, 0, 2025, 11, 2, 7, 0, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new SleeplessNightsFunction().apply(sessions);
        assertEquals(0L, result.getValue());
    }

    @Test
    void sleeplessNights_daytimeNapDoesNotCoverNight() {
        // 1 октября 23:00 – 2 октября 07:00 (охватывает 2 октября)
        // 2 октября 13:00 – 2 октября 14:00 (дневной сон не охватывает 3 октября)
        // 3 октября 23:00 – 4 октября 07:00 (охватывает 4 октября)
        // → 3 октября бессонница → 1
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 0, 2, 7, 0, SleepQuality.GOOD),
                night(2025, 10, 2, 13, 0, 2, 14, 0, SleepQuality.NORMAL),
                night(2025, 10, 3, 23, 0, 4, 7, 0, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new SleeplessNightsFunction().apply(sessions);
        assertEquals(1L, result.getValue());
    }

    // Функции определения хронотипа

    @Test
    void chronotype_emptyList_returnsDove() {
        SleepAnalysisResult result = new ChronotypeFunction().apply(List.of());
        assertEquals(Chronotype.DOVE, result.getValue());
    }

    @Test
    void chronotype_allOwlSessions_returnsOwl() {
        // Начало после 23:00, проснулся до 09:00 → OWL
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 30, 2, 10, 0, SleepQuality.GOOD),
                night(2025, 10, 2, 23, 45, 3, 10, 30, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new ChronotypeFunction().apply(sessions);
        assertEquals(Chronotype.OWL, result.getValue());
    }

    @Test
    void chronotype_allLarkSessions_returnsLark() {
        // Начало перед 22:00, проснулся перед 07:00 → LARK
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 21, 0, 2, 5, 0, SleepQuality.GOOD),
                night(2025, 10, 2, 21, 30, 3, 5, 30, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new ChronotypeFunction().apply(sessions);
        assertEquals(Chronotype.LARK, result.getValue());
    }

    @Test
    void chronotype_borderlineStartAt22_returnsDoveNotLark() {
        // Начало ровно в 22:00 → не перед 22:00 → не LARK → DOVE
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 22, 0, 2, 6, 30, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new ChronotypeFunction().apply(sessions);
        assertEquals(Chronotype.DOVE, result.getValue());
    }

    @Test
    void chronotype_tieOwlAndLark_returnsDove() {
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 30, 2, 10, 0, SleepQuality.GOOD), // OWL
                night(2025, 10, 2, 21, 0, 3, 5, 0, SleepQuality.GOOD)   // LARK
        );
        // OWL=1, LARK=1 → DOVE
        SleepAnalysisResult result = new ChronotypeFunction().apply(sessions);
        assertEquals(Chronotype.DOVE, result.getValue());
    }

    @Test
    void chronotype_daytimeNapsExcluded_onlyNightSessionsCount() {
        // Дневной сон с 13:00 до 15:00 НЕ является ночным сном → игнорируется
        // Ночной сон с 23:30 до 10:00 → OWL
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 13, 0, 1, 15, 0, SleepQuality.NORMAL),
                night(2025, 10, 1, 23, 30, 2, 10, 0, SleepQuality.GOOD)
        );
        SleepAnalysisResult result = new ChronotypeFunction().apply(sessions);
        assertEquals(Chronotype.OWL, result.getValue());
    }

    @Test
    void chronotype_owlMajority_returnsOwl() {
        List<SleepingSession> sessions = List.of(
                night(2025, 10, 1, 23, 30, 2, 10, 0, SleepQuality.GOOD),  // OWL
                night(2025, 10, 2, 23, 45, 3, 10, 30, SleepQuality.GOOD), // OWL
                night(2025, 10, 3, 22, 30, 4, 7, 30, SleepQuality.GOOD)   // DOVE
        );
        // OWL=2, DOVE=1 → OWL
        SleepAnalysisResult result = new ChronotypeFunction().apply(sessions);
        assertEquals(Chronotype.OWL, result.getValue());
    }
}
