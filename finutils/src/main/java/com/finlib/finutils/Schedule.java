package com.finlib.finutils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.temporal.TemporalAdjusters;

public final class Schedule {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final FrequencyType frequencyType;
    private final CalendarType calendarType;
    private final DayAdjustType dayAdjustType;
    private final DateGenRuleType dateGenRuleType;
    private final boolean adjustTerminationDate;
    private final boolean endOfMonthFlag;
    private final LocalDate firstDate;
    private final LocalDate nextToLastDate;
    private final List<LocalDate> adjustedDates = new ArrayList<>();

    public static class Builder {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private FrequencyType frequencyType = FrequencyType.SEMI_ANNUAL;
        private CalendarType calendarType = CalendarType.US;
        private DayAdjustType dayAdjustType = DayAdjustType.MODIFIED_FOLLOWING;
        private DateGenRuleType dateGenRuleType = DateGenRuleType.BACKWARD;
        private boolean adjustTerminationDate = true;
        private boolean endOfMonthFlag = false;
        private LocalDate firstDate = null;
        private LocalDate nextToLastDate = null;

        public Builder(LocalDate startDate, LocalDate endDate){
            if (startDate.isAfter(endDate))
                throw new IllegalArgumentException("Start Date after End Date");
            this.startDate = startDate;
            this.endDate = endDate;
        }
        public Builder withFrequency(FrequencyType frequencyType){
            this.frequencyType = frequencyType;
            return this;
        }
        public Builder withCalendar(CalendarType calendarType){
            this.calendarType = calendarType;
            return this;
        }
        public Builder withDayAdjust(DayAdjustType dayAdjustType){
            this.dayAdjustType = dayAdjustType;
            return this;
        }
        public Builder withDateGenRule(DateGenRuleType dateGenRuleType){
            this.dateGenRuleType = dateGenRuleType;
            return this;
        }
        public Builder withAdjustTerminationDate(boolean adjustTerminationDate){
            this.adjustTerminationDate = adjustTerminationDate;
            return this;
        }
        public Builder withEndOfMonthFlag(boolean endOfMonthFlag){
            this.endOfMonthFlag = endOfMonthFlag;
            return this;
        }
        public Builder withFirstDate(LocalDate firstDate){
            if (firstDate == null){
                this.firstDate = startDate;
            } else {
                if (firstDate.isAfter(startDate) && firstDate.isBefore(endDate)){
                    this.firstDate = firstDate;
                } else throw new FinlibException("First date must be after effective date and before termination date");
            }
            return this;
        }
        public Builder withNextToLastDate(LocalDate nextToLastDate){
            if (nextToLastDate == null){
                this.nextToLastDate = endDate;
            } else {
                if (nextToLastDate.isAfter(startDate) && nextToLastDate.isBefore(endDate)){
                    this.nextToLastDate = nextToLastDate;
                } else throw new FinlibException("Next to last date must be after effective date and before termination date");
            }
            return this;
        }
        public Schedule build(){
            return new Schedule(this);
        }
    }

    private Schedule(Builder builder){
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.frequencyType = builder.frequencyType;
        this.calendarType = builder.calendarType;
        this.dayAdjustType = builder.dayAdjustType;
        this.dateGenRuleType = builder.dateGenRuleType;
        this.adjustTerminationDate = builder.adjustTerminationDate;
        this.endOfMonthFlag = builder.endOfMonthFlag;
        this.firstDate = builder.firstDate;
        this.nextToLastDate = builder.nextToLastDate;
    }

    public List<LocalDate> getAdjustedDates(){
        generate();
        return Collections.unmodifiableList(adjustedDates);
    }

    public void generate(){
        int numOfMonths = 12/frequencyType.getFrequency();
        Calendar calendar = new Calendar(calendarType);
        List<LocalDate> unadjustedDates = new ArrayList<>();

        if (this.dateGenRuleType == DateGenRuleType.BACKWARD){
            LocalDate nextDate = endDate;

            while (nextDate.isAfter(startDate)) {
                unadjustedDates.add(nextDate);
                nextDate = nextDate.plusMonths(-numOfMonths);
                if (endOfMonthFlag)
                    nextDate = nextDate.with(TemporalAdjusters.lastDayOfMonth());
            }
            //Add on the Previous Coupon Date
            adjustedDates.add(nextDate);
            unadjustedDates.stream()
                    .sorted()
                    .map(x -> calendar.adjust(x, dayAdjustType))
                    .forEachOrdered(adjustedDates::add);

        } else if (this.dateGenRuleType == DateGenRuleType.FORWARD){
            LocalDate nextDate = startDate;
            while (nextDate.isBefore(endDate)) {
                unadjustedDates.add(nextDate);
                nextDate = nextDate.plusMonths(numOfMonths);
            }
            unadjustedDates.stream()
                    .sorted()
                    .map(x -> calendar.adjust(x, dayAdjustType))
                    .forEachOrdered(adjustedDates::add);

            adjustedDates.add(endDate);
        }

        if (adjustedDates.get(0).isBefore(startDate))
            adjustedDates.set(0, startDate);
        if (adjustTerminationDate)
            adjustedDates.set(adjustedDates.size()-1, calendar.adjust(endDate,dayAdjustType));
        if (adjustedDates.size() < 2)
            throw new FinlibException("Schedule has two dates only");

        LocalDate prevDt = adjustedDates.get(0);
        for (LocalDate dt : adjustedDates.subList(1, adjustedDates.size())){
            if (prevDt == dt)
                throw new FinlibException("Two matching dates in schedule");
            if (dt.isBefore(prevDt))
                throw new FinlibException("Dates are not monotonically increasing");
            prevDt = dt;
        }

    }
    @Override
    public String toString(){
        var sb = new StringBuilder();
        var df = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        sb.append("START DATE:" + df.format(startDate)).append("\n");
        sb.append("END DATE:"+ df.format(endDate)).append("\n");
        sb.append("FREQUENCY:" + frequencyType).append("\n");
        sb.append("CALENDAR:"+ calendarType).append("\n");
        sb.append("BUSDAYRULE:"+dayAdjustType).append("\n");
        sb.append("DATEGENRULE:"+dateGenRuleType).append("\n");
        sb.append("");

        if (!adjustedDates.isEmpty())
            sb.append("PCD:"+ df.format(adjustedDates.get(0))).append("\n");

        if (adjustedDates.size() > 1)
            sb.append("NCD:"+ df.format(adjustedDates.get(1))).append("\n");

        if (adjustedDates.size() > 2) {
            for (LocalDate dt: adjustedDates.subList(2, adjustedDates.size()))
                sb.append("    " + df.format(dt)).append("\n");
        }
        return sb.toString();
    }
}