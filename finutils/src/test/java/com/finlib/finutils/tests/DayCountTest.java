package com.finlib.finutils.tests;

import com.finlib.finutils.DayCount;
import com.finlib.finutils.DayCountType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class DayCountTest {
    @ParameterizedTest(name = "{index} - {1} - {2} - {3}")
    @CsvFileSource(resources = "/TestDayCount_GOLDEN.csv")
    void testDayCount(String results, String dayCountType, String start, String end, double dcf){
        DateTimeFormatter parser = new DateTimeFormatterBuilder().parseCaseInsensitive() .appendPattern("dd-MMM-yyyy").toFormatter(Locale.ENGLISH);
        LocalDate startDate = LocalDate.parse(start, parser);
        LocalDate endDate = LocalDate.parse(end, parser);
        DayCount dayCount = new DayCount(DayCountType.valueOf(dayCountType.split("\\.")[1]));//new DayCount(DayCountTypes.THIRTY_360_BOND);
        List<String> t = new ArrayList<>();
        try {
            Assertions.assertEquals(dcf,dayCount.yearFrac(startDate, endDate, Optional.of(endDate)),0.0001);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
