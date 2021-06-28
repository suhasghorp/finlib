package com.finlib.finutils.tests;

import com.finlib.finutils.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class ScheduleTest {
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("EEE dd MMM yyyy");

    private List<String> dumpSchedule(List<LocalDate> dates){
        List<String> actual = new ArrayList<>();
        LocalDate prevDate = dates.get(0);
        LocalDate effDate = dates.get(0);
        actual.add(String.format("%s,%.8f,%.8f", effDate.format(df).toUpperCase(),0.0,0.0));
        for (LocalDate dt : dates.subList(1, dates.size())) {
            double years = ChronoUnit.DAYS.between(effDate,dt)/365.0;
            double diff  = ChronoUnit.DAYS.between(prevDate,dt)/365.0;
            actual.add(String.format("%s,%.8f,%.8f", dt.format(df).toUpperCase(),years,diff));
            prevDate = dt;
        }
        return actual;
    }

    @Test
    void testSchedule(){

        LocalDate d1 = LocalDate.of(2018, 6, 20);
        LocalDate d2 = LocalDate.of(2020, 6, 20);

        // SEMI-ANNUAL BACKWARD
        List<String> expected = new ArrayList(Arrays.asList(
                "WED 20 JUN 2018,0.00000000,0.00000000",
                "THU 20 DEC 2018,0.50136986,0.50136986",
                "THU 20 JUN 2019,1.00000000,0.49863014",
                "FRI 20 DEC 2019,1.50136986,0.50136986",
                "MON 22 JUN 2020,2.00821918,0.50684932"));


        Schedule schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.SEMI_ANNUAL)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.BACKWARD).build();

        List<LocalDate> dates = schedule.getAdjustedDates();
        List<String> actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //  QUARTERLY BACKWARD///

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "WED 20 JUN 2018,0.00000000,0.00000000",
                "THU 20 SEP 2018,0.25205479,0.25205479",
                "THU 20 DEC 2018,0.50136986,0.24931507",
                "WED 20 MAR 2019,0.74794521,0.24657534",
                "THU 20 JUN 2019,1.00000000,0.25205479",
                "FRI 20 SEP 2019,1.25205479,0.25205479",
                "FRI 20 DEC 2019,1.50136986,0.24931507",
                "FRI 20 MAR 2020,1.75068493,0.24931507",
                "MON 22 JUN 2020,2.00821918,0.25753425"));

        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.QUARTERLY)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.BACKWARD).build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //  MONTHLY BACKWARD///

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "WED 20 JUN 2018,0.00000000,0.00000000",
                "FRI 20 JUL 2018,0.08219178,0.08219178",
                "MON 20 AUG 2018,0.16712329,0.08493151",
                "THU 20 SEP 2018,0.25205479,0.08493151",
                "MON 22 OCT 2018,0.33972603,0.08767123",
                "TUE 20 NOV 2018,0.41917808,0.07945205",
                "THU 20 DEC 2018,0.50136986,0.08219178",
                "MON 21 JAN 2019,0.58904110,0.08767123",
                "WED 20 FEB 2019,0.67123288,0.08219178",
                "WED 20 MAR 2019,0.74794521,0.07671233",
                "MON 22 APR 2019,0.83835616,0.09041096",
                "MON 20 MAY 2019,0.91506849,0.07671233",
                "THU 20 JUN 2019,1.00000000,0.08493151",
                "MON 22 JUL 2019,1.08767123,0.08767123",
                "TUE 20 AUG 2019,1.16712329,0.07945205",
                "FRI 20 SEP 2019,1.25205479,0.08493151",
                "MON 21 OCT 2019,1.33698630,0.08493151",
                "WED 20 NOV 2019,1.41917808,0.08219178",
                "FRI 20 DEC 2019,1.50136986,0.08219178",
                "MON 20 JAN 2020,1.58630137,0.08493151",
                "THU 20 FEB 2020,1.67123288,0.08493151",
                "FRI 20 MAR 2020,1.75068493,0.07945205",
                "MON 20 APR 2020,1.83561644,0.08493151",
                "WED 20 MAY 2020,1.91780822,0.08219178",
                "MON 22 JUN 2020,2.00821918,0.09041096"));

        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.MONTHLY)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.BACKWARD).build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //ANNUAL FORWARD///

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "WED 20 JUN 2018,0.00000000,0.00000000",
                "THU 20 JUN 2019,1.00000000,1.00000000",
                "MON 22 JUN 2020,2.00821918,1.00821918"));

        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.ANNUAL)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.FORWARD).build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //SEMI-ANNUAL FORWARD///

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "WED 20 JUN 2018,0.00000000,0.00000000",
                "THU 20 DEC 2018,0.50136986,0.50136986",
                "THU 20 JUN 2019,1.00000000,0.49863014",
                "FRI 20 DEC 2019,1.50136986,0.50136986",
                "MON 22 JUN 2020,2.00821918,0.50684932"));

        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.SEMI_ANNUAL)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.FORWARD).build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //MONTHLY FORWARD///

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "WED 20 JUN 2018,0.00000000,0.00000000",
                "FRI 20 JUL 2018,0.08219178,0.08219178",
                "MON 20 AUG 2018,0.16712329,0.08493151",
                "THU 20 SEP 2018,0.25205479,0.08493151",
                "MON 22 OCT 2018,0.33972603,0.08767123",
                "TUE 20 NOV 2018,0.41917808,0.07945205",
                "THU 20 DEC 2018,0.50136986,0.08219178",
                "MON 21 JAN 2019,0.58904110,0.08767123",
                "WED 20 FEB 2019,0.67123288,0.08219178",
                "WED 20 MAR 2019,0.74794521,0.07671233",
                "MON 22 APR 2019,0.83835616,0.09041096",
                "MON 20 MAY 2019,0.91506849,0.07671233",
                "THU 20 JUN 2019,1.00000000,0.08493151",
                "MON 22 JUL 2019,1.08767123,0.08767123",
                "TUE 20 AUG 2019,1.16712329,0.07945205",
                "FRI 20 SEP 2019,1.25205479,0.08493151",
                "MON 21 OCT 2019,1.33698630,0.08493151",
                "WED 20 NOV 2019,1.41917808,0.08219178",
                "FRI 20 DEC 2019,1.50136986,0.08219178",
                "MON 20 JAN 2020,1.58630137,0.08493151",
                "THU 20 FEB 2020,1.67123288,0.08493151",
                "FRI 20 MAR 2020,1.75068493,0.07945205",
                "MON 20 APR 2020,1.83561644,0.08493151",
                "WED 20 MAY 2020,1.91780822,0.08219178",
                "MON 22 JUN 2020,2.00821918,0.09041096"));

        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.MONTHLY)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.FORWARD).build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //SHORT STUB AT FRONT, BACKWARD //

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "MON 20 AUG 2018,0.00000000,0.00000000",
                "THU 20 SEP 2018,0.08493151,0.08493151",
                "THU 20 DEC 2018,0.33424658,0.24931507",
                "WED 20 MAR 2019,0.58082192,0.24657534",
                "THU 20 JUN 2019,0.83287671,0.25205479",
                "FRI 20 SEP 2019,1.08493151,0.25205479",
                "FRI 20 DEC 2019,1.33424658,0.24931507",
                "FRI 20 MAR 2020,1.58356164,0.24931507",
                "MON 22 JUN 2020,1.84109589,0.25753425"));

        d1 = LocalDate.of(2018, 8, 20);
        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.QUARTERLY)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.BACKWARD).build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //SUPER SHORT STUB AT FRONT, BACKWARD //

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "WED 19 SEP 2018,0.00000000,0.00000000",
                "THU 20 SEP 2018,0.00273973,0.00273973",
                "THU 20 DEC 2018,0.25205479,0.24931507",
                "WED 20 MAR 2019,0.49863014,0.24657534",
                "THU 20 JUN 2019,0.75068493,0.25205479",
                "FRI 20 SEP 2019,1.00273973,0.25205479",
                "FRI 20 DEC 2019,1.25205479,0.24931507",
                "FRI 20 MAR 2020,1.50136986,0.24931507",
                "MON 22 JUN 2020,1.75890411,0.25753425"));

        d1 = LocalDate.of(2018, 9, 19);
        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.QUARTERLY)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.BACKWARD).build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //SHORT STUB AT END, FORWARD //

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "MON 20 AUG 2018,0.00000000,0.00000000",
                "WED 20 FEB 2019,0.50410959,0.50410959",
                "TUE 20 AUG 2019,1.00000000,0.49589041",
                "THU 20 FEB 2020,1.50410959,0.50410959",
                "MON 22 JUN 2020,1.84109589,0.33698630" ));

        d1 = LocalDate.of(2018, 8, 20);
        d2 = LocalDate.of(2020, 6, 20);
        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.SEMI_ANNUAL)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.FORWARD).build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //SUPER SHORT STUB AT END, FORWARD //

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "WED 19 SEP 2018,0.00000000,0.00000000",
                "WED 19 DEC 2018,0.24931507,0.24931507",
                "TUE 19 MAR 2019,0.49589041,0.24657534",
                "WED 19 JUN 2019,0.74794521,0.25205479",
                "THU 19 SEP 2019,1.00000000,0.25205479",
                "THU 19 DEC 2019,1.24931507,0.24931507",
                "THU 19 MAR 2020,1.49863014,0.24931507",
                "FRI 19 JUN 2020,1.75068493,0.25205479",
                "MON 22 JUN 2020,1.75890411,0.00821918" ));

        d1 = LocalDate.of(2018, 9, 19);

        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.QUARTERLY)
                .withCalendar(CalendarType.TARGET)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.FORWARD).build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //TERMINATION DATE ADJUSTED, BACKWARD//

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "WED 20 JUN 2018,0.00000000,0.00000000",
                "THU 20 DEC 2018,0.50136986,0.50136986",
                "THU 20 JUN 2019,1.00000000,0.49863014",
                "FRI 20 DEC 2019,1.50136986,0.50136986",
                "MON 22 JUN 2020,2.00821918,0.50684932" ));

        d1 = LocalDate.of(2018, 6, 20);
        d2 = LocalDate.of(2020, 6, 20);

        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.SEMI_ANNUAL)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.FOLLOWING)
                .withDateGenRule(DateGenRuleType.BACKWARD)
                .withAdjustTerminationDate(true)
                .build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //END OF MONTH - NOT EOM TERM DATE - USING MOD FOLL//

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "WED 20 JUN 2018,0.00000000,0.00000000",
                "FRI 29 JUN 2018,0.02465753,0.02465753",
                "MON 31 DEC 2018,0.53150685,0.50684932",
                "FRI 28 JUN 2019,1.02191781,0.49041096",
                "TUE 31 DEC 2019,1.53150685,0.50958904",
                "MON 22 JUN 2020,2.00821918,0.47671233" ));

        d1 = LocalDate.of(2018, 6, 20);
        d2 = LocalDate.of(2020, 6, 20);

        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.SEMI_ANNUAL)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.MODIFIED_FOLLOWING)
                .withDateGenRule(DateGenRuleType.BACKWARD)
                .withAdjustTerminationDate(true)
                .withEndOfMonthFlag(true)
                .build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);

        //END OF MONTH - EOM TERM DATE - USING MOD FOLL//

        actual.clear();
        expected.clear();

        expected = new ArrayList(Arrays.asList(
                "SAT 30 JUN 2018,0.00000000,0.00000000",
                "MON 31 DEC 2018,0.50410959,0.50410959",
                "FRI 28 JUN 2019,0.99452055,0.49041096",
                "TUE 31 DEC 2019,1.50410959,0.50958904",
                "TUE 30 JUN 2020,2.00273973,0.49863014" ));

        d1 = LocalDate.of(2018, 6, 30);
        d2 = LocalDate.of(2020, 6, 30);

        schedule = new Schedule.Builder(d1,d2)
                .withFrequency(FrequencyType.SEMI_ANNUAL)
                .withCalendar(CalendarType.WEEKEND)
                .withDayAdjust(DayAdjustType.MODIFIED_FOLLOWING)
                .withDateGenRule(DateGenRuleType.BACKWARD)
                .withAdjustTerminationDate(true)
                .withEndOfMonthFlag(true)
                .build();

        dates = schedule.getAdjustedDates();
        actual = dumpSchedule(dates);

        assertIterableEquals(actual,expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    void testScheduleAlignment(boolean eomFlag){
        LocalDate valuationDate = LocalDate.of(2005, 3,29);
        LocalDate effDate = DateUtils.addTenor(valuationDate, "2d");
        FrequencyType freqType = FrequencyType.SEMI_ANNUAL;
        DayAdjustType busDayAdjustType = DayAdjustType.MODIFIED_FOLLOWING;
        DateGenRuleType dateGenRuleType = DateGenRuleType.BACKWARD;
        CalendarType calendarType = CalendarType.US;
        boolean adjustTerminationDate = false;

        LocalDate matDate1 = DateUtils.addTenor(effDate, "4Y");
        LocalDate matDate2 = DateUtils.addTenor(effDate, "50Y");
        Calendar myCal = new Calendar(calendarType);

        LocalDate adjustedMatDate1 = myCal.adjust(matDate1, busDayAdjustType);
        LocalDate adjustedMatDate2 = myCal.adjust(matDate2, busDayAdjustType);

        List<LocalDate> dates1 = new Schedule.Builder(effDate,adjustedMatDate1)
                .withFrequency(freqType)
                .withCalendar(calendarType)
                .withDayAdjust(busDayAdjustType)
                .withDateGenRule(dateGenRuleType)
                .withAdjustTerminationDate(adjustTerminationDate)
                .withEndOfMonthFlag(eomFlag).build().getAdjustedDates();

        List<LocalDate> dates2 = new Schedule.Builder(effDate,adjustedMatDate2)
                .withFrequency(freqType)
                .withCalendar(calendarType)
                .withDayAdjust(busDayAdjustType)
                .withDateGenRule(dateGenRuleType)
                .withAdjustTerminationDate(adjustTerminationDate)
                .withEndOfMonthFlag(eomFlag).build().getAdjustedDates();

        boolean match = dates1.get(dates1.size()-1).isEqual(dates2.get(dates1.size()-1));
        assertEquals(match,eomFlag);
    }

    @Test
    void testScheduleAlignmentLeapYearEOM(){
        LocalDate valuationDate = LocalDate.of(2006, 2,26);
        LocalDate effDate = DateUtils.addTenor(valuationDate, "2d");
        FrequencyType freqType = FrequencyType.SEMI_ANNUAL;
        DayAdjustType busDayAdjustType = DayAdjustType.MODIFIED_FOLLOWING;
        DateGenRuleType dateGenRuleType = DateGenRuleType.BACKWARD;
        CalendarType calendarType = CalendarType.US;
        boolean adjustTerminationDate = true;
        boolean eomFlag = true;

        LocalDate matDate1 = DateUtils.addTenor(effDate, "4Y");
        LocalDate matDate2 = DateUtils.addTenor(effDate, "50Y");
        Calendar myCal = new Calendar(calendarType);

        LocalDate adjustedMatDate1 = myCal.adjust(matDate1, busDayAdjustType);
        LocalDate adjustedMatDate2 = myCal.adjust(matDate2, busDayAdjustType);

        List<LocalDate> dates1 = new Schedule.Builder(effDate,adjustedMatDate1)
                .withFrequency(freqType)
                .withCalendar(calendarType)
                .withDayAdjust(busDayAdjustType)
                .withDateGenRule(dateGenRuleType)
                .withAdjustTerminationDate(adjustTerminationDate)
                .withEndOfMonthFlag(eomFlag).build().getAdjustedDates();

        List<LocalDate> dates2 = new Schedule.Builder(effDate,adjustedMatDate2)
                .withFrequency(freqType)
                .withCalendar(calendarType)
                .withDayAdjust(busDayAdjustType)
                .withDateGenRule(dateGenRuleType)
                .withAdjustTerminationDate(adjustTerminationDate)
                .withEndOfMonthFlag(eomFlag).build().getAdjustedDates();

        boolean match = dates1.get(dates1.size()-1).isEqual(dates2.get(dates1.size()-1));
        assertEquals(match,eomFlag);
    }

    @Test
    void testScheduleAlignmentLeapYearNotEOM(){
        LocalDate valuationDate = LocalDate.of(2006, 2,26);
        LocalDate effDate = DateUtils.addTenor(valuationDate, "2d");
        FrequencyType freqType = FrequencyType.SEMI_ANNUAL;
        DayAdjustType busDayAdjustType = DayAdjustType.MODIFIED_FOLLOWING;
        DateGenRuleType dateGenRuleType = DateGenRuleType.BACKWARD;
        CalendarType calendarType = CalendarType.US;
        boolean adjustTerminationDate = true;
        boolean eomFlag = false;

        LocalDate matDate1 = DateUtils.addTenor(effDate, "4Y");
        LocalDate matDate2 = DateUtils.addTenor(effDate, "50Y");
        Calendar myCal = new Calendar(calendarType);

        LocalDate adjustedMatDate1 = myCal.adjust(matDate1, busDayAdjustType);
        LocalDate adjustedMatDate2 = myCal.adjust(matDate2, busDayAdjustType);

        List<LocalDate> dates1 = new Schedule.Builder(effDate,adjustedMatDate1)
                .withFrequency(freqType)
                .withCalendar(calendarType)
                .withDayAdjust(busDayAdjustType)
                .withDateGenRule(dateGenRuleType)
                .withAdjustTerminationDate(adjustTerminationDate)
                .withEndOfMonthFlag(eomFlag).build().getAdjustedDates();

        List<LocalDate> dates2 = new Schedule.Builder(effDate,adjustedMatDate2)
                .withFrequency(freqType)
                .withCalendar(calendarType)
                .withDayAdjust(busDayAdjustType)
                .withDateGenRule(dateGenRuleType)
                .withAdjustTerminationDate(adjustTerminationDate)
                .withEndOfMonthFlag(eomFlag).build().getAdjustedDates();

        boolean match = dates1.get(dates1.size()-1).isEqual(dates2.get(dates1.size()-1));
        assertEquals(match,true);
    }

    @Test
    void testScheduleAlignmentEff31(){
        LocalDate valuationDate = LocalDate.of(2006, 7,29);
        LocalDate effDate = DateUtils.addTenor(valuationDate, "2d");
        FrequencyType freqType = FrequencyType.SEMI_ANNUAL;
        DayAdjustType busDayAdjustType = DayAdjustType.MODIFIED_FOLLOWING;
        DateGenRuleType dateGenRuleType = DateGenRuleType.BACKWARD;
        CalendarType calendarType = CalendarType.US;
        boolean adjustTerminationDate = true;
        boolean eomFlag = true;

        LocalDate matDate1 = DateUtils.addTenor(effDate, "4Y");
        LocalDate matDate2 = DateUtils.addTenor(effDate, "50Y");
        List<LocalDate> dates1 = new Schedule.Builder(effDate,matDate1)
                .withFrequency(freqType)
                .withCalendar(calendarType)
                .withDayAdjust(busDayAdjustType)
                .withDateGenRule(dateGenRuleType)
                .withAdjustTerminationDate(adjustTerminationDate)
                .withEndOfMonthFlag(eomFlag).build().getAdjustedDates();

        List<LocalDate> dates2 = new Schedule.Builder(effDate,matDate2)
                .withFrequency(freqType)
                .withCalendar(calendarType)
                .withDayAdjust(busDayAdjustType)
                .withDateGenRule(dateGenRuleType)
                .withAdjustTerminationDate(adjustTerminationDate)
                .withEndOfMonthFlag(eomFlag).build().getAdjustedDates();

        boolean match = dates1.get(dates1.size()-1).isEqual(dates2.get(dates1.size()-1));
        assertEquals(match,true);
    }
}

