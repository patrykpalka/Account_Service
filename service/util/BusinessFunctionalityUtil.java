package account.service.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;

public class BusinessFunctionalityUtil {

    // Get dollars and cents in correct form based on provided cents
    public static String convertSalaryToString(long salaryLong) {
        long dollars = salaryLong / 100;
        long cents = salaryLong % 100;
        return dollars + " dollar(s) " + cents + " cent(s)";
    }

    // Convert period in "MM-yyyy" format to "MonthName-yyyy"
    public static String convertPeriodToMonthName(String period) {
        // Convert "MM-yyyy" format to LocalDate object
        LocalDate date = LocalDate.parse("01-" + period, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // Get month in alphabetic format from LocalDate object
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        // Return expected period format
        return monthName + "-" + date.getYear(); // e.g., February-2021
    }

    // Populates StringBuilder with error messages insuring correct format and separation
    public static void appendStringBuilder(StringBuilder builder, String messagePrefix, String message) {
        // If errors are already in StringBuilder separate them and next one by adding ", "
        if (!builder.isEmpty()) {
            builder.append(", ");
        }

        // Add error to StringBuilder
        builder.append(messagePrefix + message);
    }

    // Checks if date is in invalid format
    public static boolean isInvalidDate(String date) {
        try {
            // Tries parsing date to LocalDate object
            LocalDate.parse("01-" + date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            // If no errors are thrown date is valid
            return false;
        } catch (DateTimeParseException e) {
            // If error is thrown date is invalid
            return true;
        }
    }
}