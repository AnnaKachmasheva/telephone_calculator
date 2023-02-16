package org.example;

import lombok.*;

import java.time.LocalDateTime;

@Data
public class PhoneCall {

    private LocalDateTime start; // start of a phone call
    private LocalDateTime end;   // end of a phone call

}
