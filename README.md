# Telephone calculator
Calculation of the amount payable for the telephone bill based on the call statement.

### Input
A string containing the call logs.
The call log is in CSV format with the following fields:
- _Phone number_ in normalized form containing only numbers (e.g. 420774567453)
- _Start of call_ in dd-MM-yyyy HH:mm:ss format
- _End of call_ in the format dd-MM-yyyy HH:mm:ss

### Output
The amount to be paid is calculated according to the entry statement according to the following rules:
- The minute rate in the interval (8:00:00, 16:00:00) is charged at CZK 1 for each started minute.
 Outside the mentioned interval, a reduced rate of CZK 0.50 applies for each started minute. 
 For each minute of the call, the starting time of the given minute is decisive for determining the rate.
- For calls longer than five minutes, a reduced rate of CZK 0.20 applies for each additional minute started after the first five minutes, 
regardless of the time the call was made.
- As part of the operator's action, it also applies that calls to the most frequently called number within the statement will not be charged. 
If the statement contains two or more such numbers, the call to the number with the arithmetically highest value will not be charged.
