package com.finlib.finutils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class DayCount {
    private DayCountType dayCountType;
    private static final int[] monthDaysNotLeapYear = new int[] {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int[] monthDaysLeapYear = new int[] {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public DayCount(DayCountType dayCountType){
        this.dayCountType = dayCountType;
    }

    public String toString(){
        return dayCountType.name();
    }

    public double yearFrac(LocalDate dt1, LocalDate dt2) {
        return this.yearFrac(dt1, dt2, Optional.empty(), FrequencyType.ANNUAL);
    }

    public double yearFrac(LocalDate dt1, LocalDate dt2, Optional<LocalDate> dt3) {
        return this.yearFrac(dt1, dt2, dt3, FrequencyType.ANNUAL);
    }

    /**
     * Calculate the year fraction between dates dt1 and dt2 using the
     * specified day count convention.
     * @param dt1
     * @param dt2
     * @return double
     */
    public double yearFrac(LocalDate dt1, LocalDate dt2, Optional<LocalDate> dt3, FrequencyType freqType) {
        double accFactor = Double.MIN_VALUE;
        int d1 = dt1.getDayOfMonth();
        int d2 = dt2.getDayOfMonth();
        int m1 = dt1.getMonthValue();
        int m2 = dt2.getMonthValue();
        int y1 = dt1.getYear();
        int y2 = dt2.getYear();
        var y3 = 0;

        if (this.dayCountType == DayCountType.THIRTY_360_BOND) {
            d1 = Math.min(d1, 30);
            if (d1 == 31 || d1 == 30)
                d2 = Math.min(d2, 30);
            double dayDiff = 360.0 * (y2 - y1) + 30.0 * (m2 - m1) + (d2 - d1);
            accFactor = dayDiff / 360.0;
        } else if (this.dayCountType == DayCountType.THIRTY_E_360) {
            d1 = Math.min(d1, 30);
            d2 = Math.min(d2, 30);
            double dayDiff = 360.0 * (y2 - y1) + 30.0 * (m2 - m1) + (d2 - d1);
            accFactor = dayDiff / 360.0;
        } else if (this.dayCountType == DayCountType.THIRTY_E_360_ISDA) {
            if (dt1.isLeapYear()) {
                if (d1 == monthDaysLeapYear[m1 - 1])
                    d1 = 30;
            } else {
                if (d1 == monthDaysNotLeapYear[m1 - 1])
                    d1 = 30;
            }
            if (dt2.isLeapYear()){
                if (d2 == monthDaysLeapYear[m2 - 1] && m2 != 2)
                    d2 = 30;
            } else {
                if (d2 == monthDaysNotLeapYear[m2 - 1] && m2 != 2)
                    d2 = 30;
            }
            double dayDiff = 360.0 * (y2 - y1) + 30.0 * (m2 - m1) + (d2 - d1);
            accFactor = dayDiff / 360.0;
        } else if (this.dayCountType == DayCountType.THIRTY_E_PLUS_360) {
            d1 = Math.min(d1, 30);
            if (d2 == 31) {
                d2 = 1;
                m2 = m2 + 1;
            }
            double dayDiff = 360.0 * (y2 - y1) + 30.0 * (m2 - m1) + (d2 - d1);
            accFactor = dayDiff / 360.0;
        } else if (this.dayCountType == DayCountType.ACT_ACT_ISDA) {
            double denom1;
            double denom2;
            if (dt1.isLeapYear())
                denom1 = 366.0;
            else denom1 = 365.0;

            if (dt2.isLeapYear())
                denom2 = 366.0;
            else denom2 = 365.0;

            if (y1 == y2)
                accFactor = ChronoUnit.DAYS.between(dt1,dt2) / denom1;
            else {
                long daysYear1 = ChronoUnit.DAYS.between(dt1, LocalDate.of(y1+1,1,1));
                long daysYear2 = ChronoUnit.DAYS.between(LocalDate.of(y1+1,1,1), dt2);
                accFactor = daysYear1 / denom1;
                accFactor = accFactor + (daysYear2 / denom2);
            }
        } else if (this.dayCountType == DayCountType.ACT_ACT_ICMA) {
            if (dt3.isPresent()) {
                long num = ChronoUnit.DAYS.between(dt1, dt2);
                long den = ChronoUnit.DAYS.between(dt1, dt3.get());
                accFactor = num * 1.0 / den;
            } else {
                throw new FinlibException("ACT_ACT_ICMA requires three dates");
            }
        } else if (this.dayCountType == DayCountType.ACT_365F) {
            accFactor = ChronoUnit.DAYS.between(dt1, dt2) / 365.0;
        } else if (this.dayCountType == DayCountType.ACT_360) {
            accFactor = ChronoUnit.DAYS.between(dt1, dt2) / 360.0;
        } else if (this.dayCountType == DayCountType.ACT_365L) {
            y3 = dt3.map(LocalDate::getYear).orElse(y2);
            long num = ChronoUnit.DAYS.between(dt1, dt2);
            LocalDate feb29;
            if (dt1.isLeapYear()) {
                feb29 = LocalDate.of(y1, 2, 29);
            } else if (LocalDate.of(y3, 1, 1).isLeapYear()) {
                feb29 = LocalDate.of(y3, 2, 29);
            } else {
                feb29 = LocalDate.of(1900, 1, 1);
            }
            var den = 1.0;
            if (freqType == FrequencyType.ANNUAL) {
                if (dt1.isBefore(feb29) && (feb29.isBefore(dt3.orElse(LocalDate.MAX)) || feb29.isEqual(dt3.orElse(LocalDate.MAX))))
                    den = 366;
            } else {
                if (LocalDate.of(y3, 1, 1).isLeapYear())
                    den = 366;
            }
            accFactor = num / den;
        } else if (this.dayCountType == DayCountType.SIMPLE) {
            long num = ChronoUnit.DAYS.between(dt1, dt2);
            accFactor = num / 365.0;
        }

        return accFactor;
    }
}
