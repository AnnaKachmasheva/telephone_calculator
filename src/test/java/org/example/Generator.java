package org.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Generator {

    public static int randomInt(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1) + min);
    }

    public static String randomPhoneNumber(int endNumber) {
        StringBuilder number = new StringBuilder();
        number.append("420");
        while (number.length() < 11) {
            number.append(randomInt(0, 9));
        }
        number.append(endNumber);
        return number.toString();
    }

    public static Phone randomPhone(int endNumber) {
        Phone phone = new Phone(randomPhoneNumber(endNumber));
        List<PhoneCall> calls = randomPhoneCallList();
        phone.setCalls(calls);
        return phone;
    }

    public static List<PhoneCall> randomPhoneCallList() {
        List<PhoneCall> calls = new ArrayList<>();
        int countPhoneCalls = randomInt(1, 9);
        for (int i = 0; i < countPhoneCalls; i++) {
            PhoneCall phoneCall = new PhoneCall();
            LocalDateTime now = LocalDateTime.now().withNano(0).withSecond(0);
            phoneCall.setStart(now.minusMinutes(randomInt(0, 1000)));
            phoneCall.setEnd(now.plusMinutes(randomInt(0, 1000)));
            calls.add(phoneCall);
        }
        return calls;
    }

    public static List<Phone> randomPhoneListWithRandomPhoneCallListSize11() {
        List<Phone> phones = new ArrayList<>();
        int countPhones = randomInt(1, 9);
        int countPhonesWithPhoneCallListSize11 = randomInt(1, countPhones);
        for (int i = 0; i < countPhones; i++) {
            phones.add(randomPhone(i));
        }
        for (int i = 0; i < countPhonesWithPhoneCallListSize11; i++) {
            phones.get(i).setCalls(phoneCallListSize11());
        }
        return phones;
    }

    public static List<Phone> randomPhoneList() {
        List<Phone> phones = new ArrayList<>();
        int countPhones = randomInt(1, 9);
        for (int i = 0; i < countPhones; i++) {
            phones.add(randomPhone(i));
        }
        return phones;
    }

    public static List<PhoneCall> phoneCallListSize11() {
        List<PhoneCall> calls = new ArrayList<>();
        int countPhoneCalls = 11;
        for (int i = 0; i < countPhoneCalls; i++) {
            PhoneCall phoneCall = new PhoneCall();
            LocalDateTime now = LocalDateTime.now().withNano(0).withSecond(0);
            phoneCall.setStart(now.minusMinutes(randomInt(0, 1000)));
            phoneCall.setEnd(now.plusMinutes(randomInt(0, 1000)));
            calls.add(phoneCall);
        }
        return calls;
    }
}
