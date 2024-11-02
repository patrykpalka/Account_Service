package account.service;

import account.exception.ExceptionWithBadRequest;
import account.model.AppUser;
import account.model.DTO.NewPasswordDTO;
import account.model.Role;
import account.repository.AppUserRepository;
import account.repository.RoleRepository;
import account.event.UserEventPublisher;
import account.service.util.BreachedPasswordChecker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final UserEventPublisher publisher;

    public ResponseEntity<?> signUp(AppUser user, Authentication auth, HttpServletRequest request) {
        // Verification steps
        // Step 1: Check if email has correct domain
        if (!user.getEmail().matches(".+@acme.com")) {
            throw new ExceptionWithBadRequest("Email must be registered with acme.com domain");
        }

        // Step 2: Check if provided password is 12 chars minimum
        if (user.getPassword().length() < 12) {
            throw new ExceptionWithBadRequest("Password length must be 12 chars minimum!");
        }

        // Step 3: Check if user isn't already registered
        AppUser userFromRepo = appUserRepository.findByEmailIgnoreCase(user.getEmail());
        if (userFromRepo != null) {
            throw new ExceptionWithBadRequest("User exist!");
        }

        // Step 4: Check if provided password is not in breached passwords list
        if (BreachedPasswordChecker.isPasswordBreached(user.getPassword())) {
            throw new ExceptionWithBadRequest("The password is in the hacker's database!");
        }
        // Verification finished

        // Encode password
        user.setPassword(encoder.encode(user.getPassword()));

        // Assign admin role to first user. Next users will have user role
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (appUserRepository.findAll().isEmpty()) {
            userRole = roleRepository.findByName("ROLE_ADMINISTRATOR");
        }
        user.getRoles().add(userRole);

        // Save user in repository
        appUserRepository.save(user);

        // Log CREATE_USER event

        // If there is no authorization assign "Anonymous" to subject. Else assign auth email to subject
        String subject = (auth != null && auth.getName() != null) ? auth.getName() : "Anonymous";

        String object = user.getEmail();
        String path = request.getRequestURI();

        publisher.publishEvent("CREATE_USER", subject, object, path);

        // Return user in response body with 200 code
        return ResponseEntity.ok(user);
    }

    public ResponseEntity<?> changePassword(NewPasswordDTO newPasswordBody, Authentication auth, HttpServletRequest request) {
        // Fetch password from provided body
        String newPassword = newPasswordBody.getNewPassword();

        // Verification steps
        // Step 1: Check if provided password is 12 chars minimum
        if (newPassword.length() < 12) {
            throw new ExceptionWithBadRequest("Password length must be 12 chars minimum!");
        }

        // Find user by email from authentication
        AppUser currentUser = appUserRepository.findByEmailIgnoreCase(auth.getName());

        // Set current password to variable for later use
        String oldPassword = currentUser.getPassword();

        // Step 2: Check if passwords are not the same
        if (encoder.matches(newPassword, oldPassword)) {
            throw new ExceptionWithBadRequest("The passwords must be different!");
        }

        // Step 3: Check if provided password is not in breached passwords list
        if (BreachedPasswordChecker.isPasswordBreached(newPassword)) {
            throw new ExceptionWithBadRequest("The password is in the hacker's database!");
        }

        // Encode password and save it in repository
        currentUser.setPassword(encoder.encode(newPassword));
        appUserRepository.save(currentUser);

        // Prepare response body
        Map<String, String> responseBody = new LinkedHashMap<>();
        responseBody.put("email", currentUser.getEmail());
        responseBody.put("status", "The password has been updated successfully");

        // Log CHANGE_PASSWORD event
        String email = auth.getName();
        String path = request.getRequestURI();

        publisher.publishEvent("CHANGE_PASSWORD", email, email, path);

        // Return response body with 200 code
        return ResponseEntity.ok(responseBody);
    }
}