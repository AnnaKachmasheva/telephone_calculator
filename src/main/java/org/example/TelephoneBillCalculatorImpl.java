package org.example;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    private static final double BASE_RATE = 1;             // minute rate in interval (8:00:00, 16:00:00)
    private static final double REDUCED_RATE = 0.5;        // minute rate outside interval (8:00:00, 16:00:00)
    private static final double PROMO_RATE = 0;            // calls within the promotion are free
    private static final double DISCOUNT = 0.2;            // discount if the conversation lasts more than 5 minutes

    private static final int MINUTES_UNTIL_DISCOUNT = 5;

    private static final String PATTERN_DATE_TIME = "dd-MM-yyyy HH:mm:ss";

    private static final String REGEX_NEW_LINE = "\n";
    private static final String REGEX_COMMA = ",";


    @Override
    public BigDecimal calculate(String phoneLog) {
        List<Phone> phones = getAllPhones(phoneLog);
        Phone promoPhone = getPromoPhone(phones);
        String promoPhoneNumber = promoPhone.getNumber();

        double sum = 0; // amount payable for all calls
        for (Phone phone : phones) {
            String phoneNumber = phone.getNumber();
            double costCalls = calculatePhone(phone);
            sum += (phoneNumber.equals(promoPhoneNumber) ? PROMO_RATE * costCalls : costCalls);
        }

        return new BigDecimal((int) sum);
    }

    /**
     * Parses a string containing logs.
     * The logs are separated from each other with "\n".
     * Each log looks like "phone_number,call_start_date call_start_time,call_end_date call_end_time"
     * e.g. "420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00".
     *
     * @param phoneLog is the string containing the call logs
     * @return list of Phone objects
     */
    public List<Phone> getAllPhones(String phoneLog) {
        List<Phone> phones = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_DATE_TIME);

        String[] logs = phoneLog.split(REGEX_NEW_LINE);
        for (String log : logs) {
            if (log.equals(""))
                break;

            String[] logParts = log.split(REGEX_COMMA);
            String phoneNumber = logParts[0];
            LocalDateTime start = LocalDateTime.parse(logParts[1], formatter);
            LocalDateTime end = LocalDateTime.parse(logParts[2], formatter);

            PhoneCall call = new PhoneCall();
            call.setStart(start);
            call.setEnd(end);

            Optional<Phone> phoneOptional = (phones.isEmpty() ?
                                                    Optional.empty() :
                                                    phones.stream().
                                                            filter(p -> p.getNumber().equals(phoneNumber)).
                                                            findFirst());
            Phone phone;
            if (phoneOptional.isEmpty()) {
                phone = new Phone(phoneNumber);
                phones.add(phone);
            } else {
                phone = phoneOptional.get();
            }

            phone.addPhoneCall(call);
        }
        return phones;
    }

    /**
     * If the phone list is empty then returns null.
     * If there is only 1 Phone in the list with the longest list of PhoneCalls, then it is returned.
     * If there are several such Phones, then the one with the largest arithmetic value of the Phone number is returned.
     *
     * @param allPhones is list of Phone objects
     * @return Phone that matches the conditions of the promotion
     */
    public Phone getPromoPhone(List<Phone> allPhones) {
        List<Phone> phones = getMostFrequentlyCalledPhones(allPhones); // Phones with the largest PhoneCall lists

        if (phones.isEmpty())
            return null;

        Phone phoneWithTheLargestNumber = phones.get(0);
        for (Phone phone : phones) {
            long maxPhoneNumber = Long.parseLong(phoneWithTheLargestNumber.getNumber());
            long phoneNumber = Long.parseLong(phone.getNumber());
            if (phoneNumber > maxPhoneNumber)
                phoneWithTheLargestNumber = phone;
        }

        return phoneWithTheLargestNumber;
    }

    /**
     * From the list of Phones returns only those that have the longest list of PhoneCalls.
     * If the phone list is empty it returns empty List.
     *
     * @param phones list of Phone objects
     * @return list of Phone objects with the longest list of CallPhone or empty List
     */
    public List<Phone> getMostFrequentlyCalledPhones(List<Phone> phones) {
        if (phones == null)
            return Collections.emptyList();

        // get max count calls
        var maxCountOption = phones.stream()
                .map(p -> p.getCalls().size())
                .max(Comparator.comparingInt(a -> a));

        return maxCountOption.map(integer -> phones.stream()
                        .filter(p -> p.getCalls().size() == integer)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * Calculation of the cost of all calls of this phone.
     * If the duration of the call exceeds 'MINUTES_UNTIL_DISCOUNT' minutes,
     * then each next minute has a rate 'DISCOUNT'.
     * Minutes before "MINUTES_UNTIL_DISCOUNT" have no discount rate.
     *
     * @param phone to which all calls are calculated
     * @return cost of all calls
     */
    public double calculatePhone(Phone phone) {
        double sum = 0;
        for (PhoneCall call : phone.getCalls()) {
            LocalDateTime callStart = call.getStart();
            LocalDateTime callEnd = call.getEnd();

            long callDuration = ChronoUnit.MINUTES.between(callStart, callEnd); // call duration in minutes
            if (callDuration <= MINUTES_UNTIL_DISCOUNT) {
                sum += calculateWithoutDiscount(callStart, callEnd);
            } else {
                LocalDateTime startWithoutDiscount = LocalDateTime.from(callStart);
                callStart = callStart.plusMinutes(MINUTES_UNTIL_DISCOUNT);
                LocalDateTime endWithoutDiscount = LocalDateTime.from(callStart);

                // without discount 'DISCOUNT"
                sum += calculateWithoutDiscount(startWithoutDiscount, endWithoutDiscount);

                // with discount 'DISCOUNT'
                long duration = ChronoUnit.MINUTES.between(callStart, callEnd);
                sum += DISCOUNT * duration;
            }
        }

        return sum;
    }

    /**
     * Call interval cost calculation.
     * The cost in the interval from (8:00:00, 16:00:00) is calculated at the rate of 'BASE_RATE'.
     * The rest of the time, the rate is 'REDUCED_RATE'.
     *
     * @param callStart date and time of the beginning of the call interval
     * @param callEnd   date and time of the end of the call interval
     * @return call interval time cost
     */
    public double calculateWithoutDiscount(LocalDateTime callStart, LocalDateTime callEnd) {
        double sum = 0;

        // call start and call end within one day
        LocalTime startTime;
        LocalTime endTime;

        // the beginning and end of the time interval when the reduced rate applies
        LocalTime startInterval = LocalTime.of(8, 0, 0);
        LocalTime endInterval = LocalTime.of(16, 0, 0);

        long day = 0; // day number
        long amountOfDays = ChronoUnit.DAYS.between(callStart, callEnd); // number of days during which the call is made
        if ((callStart.getDayOfYear() != callEnd.getDayOfYear()))
            amountOfDays += 1;

        while (day <= amountOfDays) {
            startTime = ((day == 0) ? callStart.toLocalTime() : LocalTime.of(0, 0, 0));
            endTime = ((day == amountOfDays) ? callEnd.toLocalTime() : LocalTime.of(23, 59, 59));

            // calculation of the intersection of 2 intervals (startTime, endTime) and (startInterval, endInterval)
            LocalTime start = (startTime.isAfter(startInterval) ? startTime : startInterval);
            LocalTime end = (endTime.isBefore(endInterval) ? endTime : endInterval);

            //call duration during the day in minutes
            long callDurationDay = ChronoUnit.MINUTES.between(startTime, endTime);

            // number of minutes with reduced rate
            long minutesWithoutBaseRate = ChronoUnit.MINUTES.between(start, end);
            if (minutesWithoutBaseRate < 0) minutesWithoutBaseRate = 0;
            long minutesWithReducedRate = callDurationDay - minutesWithoutBaseRate;

            sum += minutesWithReducedRate * REDUCED_RATE + minutesWithoutBaseRate * BASE_RATE;
            day++;
        }
        return sum;
    }

}