package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TelephoneBillCalculatorImplTest {

    private static final String PATH_CSV = "./src/test/resources/logs.csv";

    private TelephoneBillCalculatorImpl telephoneBillCalculator = new TelephoneBillCalculatorImpl();

    private static List<Phone> phones;

    // create csvFile
    @BeforeAll
    public static void init() {
        phones = Generator.randomPhoneList();
        try {
            File csvFile = new File(PATH_CSV);
            if (csvFile.exists()) {
                csvFile.delete();
                csvFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(csvFile);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            for (Phone phone : phones) {
                for (PhoneCall call : phone.getCalls()) {
                    StringBuilder line = new StringBuilder();
                    line.append(phone.getNumber())
                            .append(",")
                            .append(call.getStart().format(formatter))
                            .append(",")
                            .append(call.getEnd().format(formatter));
                    line.append("\n");
                    fileWriter.write(line.toString());
                }
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // getAllPhones tests

    @Test
    void getAllPhones_PhonesList() throws IOException {
        List<Phone> resultPhones = telephoneBillCalculator.getAllPhones(Files.readString(Paths.get(PATH_CSV)));

        assertEquals(phones.size(), resultPhones.size());
        for (int i = 0; i < resultPhones.size(); i++) {
            assertEquals(phones.get(i).getNumber(), resultPhones.get(i).getNumber());
            assertEquals(phones.get(i).getCalls().size(), resultPhones.get(i).getCalls().size());
            for (int j = 0; j < resultPhones.get(i).getCalls().size(); j++) {
                assertEquals(phones.get(i).getCalls().get(j).getStart(), resultPhones.get(i).getCalls().get(j).getStart());
                assertEquals(phones.get(i).getCalls().get(j).getEnd(), resultPhones.get(i).getCalls().get(j).getEnd());
            }
        }
    }

    @Test
    void getAllPhones_EmptyString_PhoneListIsEmpty() {
        List<Phone> resultPhones = telephoneBillCalculator.getAllPhones("");

        assertTrue(resultPhones.isEmpty());
    }

    // getMostFrequentlyCalledPhones tests

    @Test
    void getMostFrequentlyCalledPhones_EmptyList_EmptyList() {
        List<Phone> phonesResultList = telephoneBillCalculator.getMostFrequentlyCalledPhones(new ArrayList<>());

        assertTrue(phonesResultList.isEmpty());
    }

    @Test
    void getMostFrequentlyCalledPhones_PhoneList() {
        List<Phone> phonesResultList = telephoneBillCalculator
                .getMostFrequentlyCalledPhones(Generator.randomPhoneListWithRandomPhoneCallListSize11());

        for (Phone phone : phonesResultList)
            assertEquals(11, phone.getCalls().size());
    }

    // getPromoPhone tests

    @Test
    void getPromoPhone_EmptyList_Null() {
        Phone promoPhoneResult = telephoneBillCalculator.getPromoPhone(new ArrayList<>());

        assertNull(promoPhoneResult);
    }

    @Test
    void getPromoPhone_PhoneListWithEmptyCallLists_Null() {
        List<Phone> phoneList = Generator.randomPhoneList();
        for (Phone phone : phoneList)
            phone.setCalls(Collections.emptyList());

        Phone promoPhoneResult = telephoneBillCalculator.getPromoPhone(new ArrayList<>());

        assertNull(promoPhoneResult);
    }

    @Test
    void getPromoPhone_PhoneListWithOnePromoPhone_ListWithOnePhone() {
        List<Phone> phoneList = Generator.randomPhoneList();

        Phone expectedPhone = phoneList.get(0);
        expectedPhone.setCalls(Generator.phoneCallListSize11());

        Phone resultPhone = telephoneBillCalculator.getPromoPhone(phoneList);

        assertEquals(expectedPhone.getNumber(), resultPhone.getNumber());
        assertEquals(expectedPhone.getCalls().size(), resultPhone.getCalls().size());
    }

    @Test
    void getPromoPhone_PhoneListWithTwoPromoPhone_PhoneWithArithmeticallyHighestValue() {
        List<Phone> phoneList = Generator.randomPhoneList();

        Phone expectedPhone = phoneList.get(0);
        expectedPhone.setCalls(Generator.phoneCallListSize11());

        Phone phone = new Phone("42000000000");
        phone.setCalls(Generator.phoneCallListSize11());
        phoneList.add(phone);

        Phone phoneResult = telephoneBillCalculator.getPromoPhone(phoneList);

        assertEquals(expectedPhone.getNumber(), phoneResult.getNumber());
        assertEquals(expectedPhone.getCalls().size(), phoneResult.getCalls().size());
    }

    // calculateWithoutDiscount test

    @ParameterizedTest
    @CsvFileSource(files = "./src/test/resources/call_interval.csv")
    void calculateWithoutDiscount(String startStr, String endStr, String resultStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startStr, formatter);
        LocalDateTime end = LocalDateTime.parse(endStr, formatter);

        double expected = Double.parseDouble(resultStr);
        double result = telephoneBillCalculator.calculateWithoutDiscount(start, end);

        assertEquals(expected, result);
    }

    // calculatePhone tests

    @Test
    void calculatePhone_PhoneNoCalls_0() {
        Phone phone = Generator.randomPhone(1);
        phone.setCalls(Collections.emptyList());

        double resultCost = telephoneBillCalculator.calculatePhone(phone);

        assertEquals(0, resultCost);
    }

    @Test
    void calculatePhone() {
        Phone phone = Generator.randomPhone(1);
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 0));
        PhoneCall call = new PhoneCall();
        call.setStart(start);
        call.setEnd(end);
        phone.setCalls(List.of(call));

        double expectedCost = 289.3;
        double resultCost = telephoneBillCalculator.calculatePhone(phone);

        assertEquals(expectedCost, resultCost);
    }

}