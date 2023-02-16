package org.example;

import java.math.BigDecimal;

public interface TelephoneBillCalculator {

    /**
     * The call log is in CSV format with the following fields:
     * - Phone number in normalized form containing only numbers (e.g. 420774567453)
     * - Start of call in dd-MM-yyyy HH:mm:ss format
     * - End of call in the format dd-MM-yyyy HH:mm:ss
     * Example: 420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
     *
     * @param phoneLog containing the call log
     * @return amount required to pay
     */
    BigDecimal calculate(String phoneLog);

}

