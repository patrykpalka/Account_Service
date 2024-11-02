package account.controller;

import account.model.DTO.RoleChangeDTO;
import account.model.DTO.UserBlockedStatusChangeDTO;
import account.service.ServiceFunctionalityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class ServiceFunctionalityController {

    private final ServiceFunctionalityService service;

    @Autowired
    public ServiceFunctionalityController(ServiceFunctionalityService service) {
        this.service = service;
    }

    @PutMapping("/api/admin/user/role")
    public ResponseEntity<?> changeRole(@Valid @RequestBody RoleChangeDTO body, BindingResult result, Authentication auth, HttpServletRequest request) {
        // Handle invalid input manually (optional)
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        return service.changeRole(body, auth, request);
    }

    @DeleteMapping("/api/admin/user/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email, Authentication auth, HttpServletRequest request) {
        return service.deleteUser(email, auth, request);
    }

    @GetMapping("/api/admin/user/")
    public ResponseEntity<?> displayInformation() {
        return service.displayInformation();
    }

    @PutMapping("/api/admin/user/access")
    public ResponseEntity<?> changeUserBlockedStatus(@Valid @RequestBody UserBlockedStatusChangeDTO body, BindingResult result, Authentication auth, HttpServletRequest request) {
        // Handle invalid input manually (optional)
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        return service.changeUserBlockedStatus(body, auth, request);
    }
}