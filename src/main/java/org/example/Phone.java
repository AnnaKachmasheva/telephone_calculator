package org.example;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class Phone {

    private String number; // phone number, e.g. "420774577453"
    private List<PhoneCall> calls;

    public Phone(String number) {
        this.number = number;
        this.calls = new ArrayList<>();
    }

    public void addPhoneCall(PhoneCall call) {
        Objects.requireNonNull(call);
        calls.add(call);
    }

}
