package account.controller;

import account.model.AppUser;
import account.model.DTO.NewPasswordDTO;
import account.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody AppUser user, BindingResult result, Authentication auth, HttpServletRequest request) {
        // Handle invalid input manually (optional)
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        return service.signUp(user, auth, request);
    }

    @PostMapping("/api/auth/changepass")
    public ResponseEntity<?> changePass(@RequestBody NewPasswordDTO newPasswordBody, Authentication auth, HttpServletRequest request) {
        return service.changePassword(newPasswordBody, auth, request);
    }
}