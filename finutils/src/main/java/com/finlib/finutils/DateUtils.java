package com.finlib.finutils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class DateUtils {

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
                return newDate.plusMonths(numOfPeriods);
            case "Y":
                return newDate.plusYears(numOfPeriods);
            default:
                throw new FinlibException("Unknown Tenor");
        }
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
        var dStart = 14;
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

}
