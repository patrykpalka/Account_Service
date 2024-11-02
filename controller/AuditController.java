package account.controller;

import account.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/api/security/events/")
    public ResponseEntity<?> getListOfEvents() {
        return auditService.getListOfEvents();
    }
}
