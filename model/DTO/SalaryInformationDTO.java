package account.model.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class SalaryInformationDTO implements Comparable<SalaryInformationDTO> {

    private String name;

    private String lastname;

    @JsonIgnore
    private String periodNumericMonth; // Field used only for comparison

    private String period;

    private String salary;

    @Override
    public int compareTo(SalaryInformationDTO other) {
        // Prepare formatter of correct pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Convert DTO objects' period to LocalDate
        LocalDate thisDate = LocalDate.parse("01-" + periodNumericMonth, formatter);
        LocalDate otherDate = LocalDate.parse("01-" + other.periodNumericMonth, formatter);

        // Sort in descending order by ensuring that other date is compared to this date, not reverse
        return otherDate.compareTo(thisDate);
    }
}