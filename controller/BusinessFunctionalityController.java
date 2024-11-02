package account.controller;

import account.model.Payments;
import account.service.BusinessFunctionalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class BusinessFunctionalityController {

    private final BusinessFunctionalityService service;

    @Autowired
    public BusinessFunctionalityController(BusinessFunctionalityService businessFunctionalityService) {
        this.service = businessFunctionalityService;
    }

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> getPayment(@RequestParam Optional<String> period, Authentication auth) {
        return service.getPayment(period, auth);
    }

    @PostMapping("/api/acct/payments")
    public ResponseEntity<?> uploadPayments(@RequestBody List<Payments> payments) {
        return service.uploadPayments(payments);
    }

    @PutMapping("/api/acct/payments")
    public ResponseEntity<?> updatePayments(@RequestBody Payments payment) {
        return service.updatePayment(payment);
    }
}