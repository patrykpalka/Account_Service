package account.service;

import account.exception.ExceptionWithBadRequest;
import account.model.AppUser;
import account.model.Payments;
import account.model.DTO.SalaryInformationDTO;
import account.repository.AppUserRepository;
import account.repository.PaymentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static account.service.util.BusinessFunctionalityUtil.*;

@Service
public class BusinessFunctionalityService {

    private final AppUserRepository appUserRepository;
    private final PaymentsRepository paymentsRepository;

    @Autowired
    public BusinessFunctionalityService(AppUserRepository appUserRepository, PaymentsRepository paymentsRepository) {
        this.appUserRepository = appUserRepository;
        this.paymentsRepository = paymentsRepository;
    }

    @Transactional
    public ResponseEntity<?> getPayment(Optional<String> period, Authentication auth) {
        // Get current user
        AppUser user = appUserRepository.findByEmailIgnoreCase(auth.getName());

        // If period is specified in parameter return single information
        if (period.isPresent()) {
            return getSalaryInformationForPeriod(period.get(), user);
        }

        // Else if period is not specified in parameter return list of information
        return getAllSalaryInformation(user);
    }

    private ResponseEntity<?> getSalaryInformationForPeriod (String period, AppUser user) {
        // Check if data format is correct
        if (isInvalidDate(period)) {
            throw new ExceptionWithBadRequest("Date format is wrong");
        }

        // Get requested payment
        Payments payment = paymentsRepository.findByEmployeeAndPeriod(user.getEmail(), period);

        // If no payment was found return empty body
        if (payment == null) {
            return ResponseEntity.ok(Map.of());
        }

        // Else prepare variables to output form
        String periodOutput = convertPeriodToMonthName(period);
        String salary = convertSalaryToString(payment.getSalary());

        // Prepare response body
        SalaryInformationDTO responseBody = new SalaryInformationDTO(
                user.getName(), user.getLastname(), payment.getPeriod(), periodOutput, salary);

        return ResponseEntity.ok(responseBody);
    }

    private ResponseEntity<?> getAllSalaryInformation(AppUser user) {
        // Find all payments matching current user
        List<Payments> listOfPayments = paymentsRepository.findAllByEmployee(user.getEmail());

        // Iterate over all payments matching current user and get salary information list in final form
        List<SalaryInformationDTO> salaryInformationList = listOfPayments.stream()
                .map(payment -> new SalaryInformationDTO(
                        user.getName(),
                        user.getLastname(),
                        payment.getPeriod(),
                        convertPeriodToMonthName(payment.getPeriod()),
                        convertSalaryToString(payment.getSalary())
                ))
                .collect(Collectors.toList());

        // If no payment was found return empty array body
        if (salaryInformationList.isEmpty()) {
            return ResponseEntity.ok(List.of(Map.of()));
        }

        // Else sort payments in descending order
        salaryInformationList = salaryInformationList.stream()
                .sorted()
                .collect(Collectors.toList());

        return ResponseEntity.ok(salaryInformationList);
    }

    @Transactional
    public ResponseEntity<?> uploadPayments(List<Payments> payments) {

        // Preparation for checks

        // Initialize StringBuilder for storing potential errors
        StringBuilder errors = new StringBuilder();

        // Prepare HashSet to store potential duplicates in the request body
        Set<String> duplicateChecker = new HashSet<>();



        // Fetch all users and store them in a map
        Map<String, AppUser> users = appUserRepository.findAll().stream()
                .collect(Collectors.toMap(AppUser::getEmail, user -> user, (existing, replacement) -> existing));

        // Fetch all existing payments for the provided employees and periods
        List<Payments> existingPayments = paymentsRepository.findAllByEmployeeInAndPeriodIn(
                payments.stream().map(Payments::getEmployee).collect(Collectors.toList()),
                payments.stream().map(Payments::getPeriod).collect(Collectors.toList()));

        // Create a map for quick lookup of existing payments
        Set<String> existingPaymentSet = existingPayments.stream()
                .map(p -> p.getEmployee() + "-" + p.getPeriod())
                .collect(Collectors.toSet());



        // Checks

        // Iterate over each payment provided in request body
        for (Payments payment : payments) {
            // Prepare salary in correct form for error messages
            String salaryAsString = convertSalaryToString(payment.getSalary());

            // Prepare error message prefix for current payment
            String format = "(Employee: %s, period: %s, salary: %s): ";
            String currentPayment = String.format(format, payment.getEmployee(), payment.getPeriod(), salaryAsString);

            // Check if salary is negative
            if (payment.getSalary() <= 0) {
                appendStringBuilder(errors, currentPayment, "Salary can't be negative");
            }

            // Check if data format is correct
            if (isInvalidDate(payment.getPeriod())) {
                appendStringBuilder(errors, currentPayment, "Date format is wrong");
            }

            // Check for duplicates in the request body
            String key = payment.getEmployee() + "-" + payment.getPeriod();
            if (!duplicateChecker.add(key)) {
                appendStringBuilder(errors, currentPayment, "Duplicate payment in the request body");
            }

            // Check if user is present in the database
            if (!users.containsKey(payment.getEmployee())) {
                appendStringBuilder(errors, currentPayment, "User is not present in the database");
            }

            // Check if payment already exists in the database for the same period
            if (existingPaymentSet.contains(key)) {
                appendStringBuilder(errors, currentPayment, "Payment was already allocated");
            }
        }

        // If there are errors, throw an exception and rollback the transaction
        if (!errors.isEmpty()) {
            throw new ExceptionWithBadRequest(errors.toString());
        }

        // Save all payments if there are no errors
        paymentsRepository.saveAll(payments);

        return ResponseEntity.ok(Map.of("status", "Added successfully!"));
    }

    @Transactional
    public ResponseEntity<?> updatePayment(Payments payment) {
        // Get payment from database based on email and period from provided body
        Payments existingPayment = paymentsRepository.findByEmployeeAndPeriod(payment.getEmployee(), payment.getPeriod());

        // If such payment doesn't exist throw error and stop program
        if (existingPayment == null) {
            throw new ExceptionWithBadRequest("Payment for the specified employee and period does not exist!");
        }

        // Change salary in temporary object
        existingPayment.setSalary(payment.getSalary());

        // Save changed payment object to database
        paymentsRepository.save(existingPayment);

        return ResponseEntity.ok(Map.of("status", "Updated successfully!"));
    }
}