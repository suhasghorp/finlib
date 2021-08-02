package com.finlib.finutils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

public final class DateUtils {

    private static final int[] monthDaysLeapYear = new int[]{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int[] monthDaysNotLeapYear = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private DateUtils(){}

    public static LocalDate addTenor(LocalDate dt, String tenor) {
        tenor = tenor.toUpperCase();
        var periodType = tenor.substring(tenor.length() - 1);
        var numOfPeriods = Integer.parseInt(tenor.substring(0, tenor.length()-1));
        LocalDate newDate = dt;
        switch (periodType.toUpperCase()){
            case "D":
                return newDate.plusDays(1 * (long)numOfPeriods);
            case "W":
                return newDate.plusDays(7 * (long)numOfPeriods);
            case "M":
                for (int x =0; x < numOfPeriods;x++)
                    newDate = newDate.plusMonths(1);
                return newDate;
            case "Y":
                return newDate.plusYears(numOfPeriods);
            default:
                throw new FinlibException("Unknown Tenor");
        }
    }

    public static LocalDate addWeekDays(LocalDate dt, int numDays){
        LocalDate newDate;
        int numWeeks = Math.abs(numDays) / 5 ;
        int remainingDays = numDays % 5;

        if (numDays > 0) {
            if ((dt.getDayOfWeek().getValue()-1) + remainingDays > (DayOfWeek.FRIDAY.getValue()-1))
                remainingDays += 2;
            newDate = dt.plusDays(numWeeks * 7 + remainingDays);
        } else {
            if ((dt.getDayOfWeek().getValue()-1) - remainingDays < (DayOfWeek.MONDAY.getValue()-1))
                remainingDays += 2;
            newDate = dt.plusDays(-(numWeeks * 7 + remainingDays));
        }
        return newDate;
    }

    public static LocalDate addWorkdays(LocalDate dt, int numDays){
        LocalDate newDate = dt;
        while (numDays > 0) {
            newDate = newDate.plusDays(1);
            if (newDate.getDayOfWeek() != DayOfWeek.SATURDAY || newDate.getDayOfWeek() != DayOfWeek.SUNDAY){
                numDays = numDays - 1;
            }
        }
        if (numDays < 0) {
            while (numDays < 0){
                newDate = newDate.plusDays(-1);
                if (newDate.getDayOfWeek() != DayOfWeek.SATURDAY || newDate.getDayOfWeek() != DayOfWeek.SUNDAY){
                    numDays = numDays + 1;
                }
            }
        }
        return newDate;
    }
    public static LocalDate nextIMMDate(LocalDate dt) {
        int y = dt.getYear();
        int m = dt.getMonthValue();
        int d = dt.getDayOfMonth();

        int yImm = y;
        var mImm = 0;

        if (m == 12 && d >= thirdWednesdayOfMonth(m, y)) {
            mImm = 3;
            yImm = y + 1;
        } else if (m == 10 || m == 11 || m == 12){
            mImm = 12;
        } else if (m == 9 && d >= thirdWednesdayOfMonth(m, y)){
            mImm = 12;
        } else if (m == 7 || m == 8 || m == 9){
            mImm = 9;
        } else if (m == 6 && d >= thirdWednesdayOfMonth(m, y)){
            mImm = 9;
        } else if (m == 4 || m == 5 || m == 6){
            mImm = 6;
        } else if (m == 3 && d >= thirdWednesdayOfMonth(m, y)){
            mImm = 6;
        } else if (m == 1 || m == 2 || m == 3){
            mImm = 3;
        }
        int dImm = thirdWednesdayOfMonth(mImm, yImm);
        return LocalDate.of(yImm, mImm, dImm);
    }

    /*For a specific month and year this returns the day number of the
        3rd Wednesday by scanning through dates in the third week.*/
    public static int thirdWednesdayOfMonth(int m, int y) {
        var dStart = 15;
        var dEnd = 21;
        for (int d = dStart; d < dEnd; d++) {
            if (LocalDate.of(y, m, d).getDayOfWeek() == DayOfWeek.WEDNESDAY)
                return d;
        }
        //Should never reach this line but just to be defensive
        throw new FinlibException("Third Wednesday not found");
    }

    public static LocalDate addYears(LocalDate startDate, double years){
        double daysInMonth = 365.242/12.0;
        int mmi = (int)(years * 12.0);
        int ddi = (int)((years * 12.0 - mmi) * daysInMonth);
        LocalDate newDt = startDate.plusMonths(mmi);
        newDt = newDt.plusDays(ddi);
        return newDt;
    }

    public static double timeFromDate(LocalDate dt, LocalDate startDate, DayCountType dayCount) {
        if (dayCount == DayCountType.SIMPLE){
            return ChronoUnit.DAYS.between(dt, startDate)/365.0;
        } else {
            var ret = 1e-30;
            try{
                ret = new DayCount(dayCount).yearFrac(startDate,dt, Optional.empty());
            }catch(Exception ex){
                ex.printStackTrace();
            }
            return ret;
        }
    }

    public static LocalDate addMonths(LocalDate dt, int numMonths){
        int d = dt.getDayOfMonth();
        int m = dt.getMonthValue() + numMonths;
        int y = dt.getYear();

        while (m > 12) {
            m = m - 12;
            y += 1;
        }
        while (m < 1) {
            m = m + 12;
            y -= 1;
        }

        if (dt.isLeapYear()) {
            if (d > monthDaysLeapYear[m - 1])
                d = monthDaysLeapYear[m - 1];
        } else {
            if (d > monthDaysNotLeapYear[m - 1])
                d = monthDaysNotLeapYear[m - 1];
        }
        return LocalDate.of(y,m,d);
    }

    public static LocalDate nextCDSDate(LocalDate startDate, Optional<Integer> months){

        //Returns a CDS date that is mm months after the Date. If no
        //argument is supplied then the next CDS date after today is returned.
        int mm = months.orElse(0);
        LocalDate nextDate = addMonths(startDate, mm);

        int y = nextDate.getYear();
        int m = nextDate.getMonthValue();
        int d = nextDate.getDayOfMonth();

        int d_cds = 20;
        int y_cds = nextDate.getYear();
        int m_cds = 999;

        if (m == 12 && d >= 20) {
            m_cds = 3;
            y_cds = y + 1;
        } else if (m == 10 || m == 11 || m == 12) {
            m_cds = 12;
        } else if (m == 9 && d >= 20) {
            m_cds = 12;
        } else if (m == 7 || m == 8 || m == 9) {
            m_cds = 9;
        } else if (m == 6 && d >= 20) {
            m_cds = 9;
        } else if (m == 4 || m == 5 || m == 6) {
            m_cds = 6;
        } else if (m == 3 && d >= 20) {
            m_cds = 6;
        } else if (m == 1 || m == 2 || m == 3) {
            m_cds = 3;
        }
        return LocalDate.of(y_cds, m_cds, d_cds);
    }

}
