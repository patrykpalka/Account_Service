package account.service;

import account.event.UserEventPublisher;
import account.exception.ExceptionWithBadRequest;
import account.exception.ExceptionWithNotFound;
import account.model.AppUser;
import account.model.DTO.RoleChangeDTO;
import account.model.DTO.UserBlockedStatusChangeDTO;
import account.model.Role;
import account.repository.AppUserRepository;
import account.repository.LoginAttemptRepository;
import account.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServiceFunctionalityService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final LoginAttemptRepository attemptRepository;
    private final UserEventPublisher publisher;

    @Autowired
    public ServiceFunctionalityService(AppUserRepository appUserRepository, RoleRepository roleRepository, LoginAttemptRepository attemptRepository, UserEventPublisher publisher) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.attemptRepository = attemptRepository;
        this.publisher = publisher;
    }

    public ResponseEntity<?> displayInformation() {
        // Load users from repository
        List<AppUser> users = appUserRepository.findAll();

        // If no users are present in repository return empty body
        if (users.isEmpty()) {
            return ResponseEntity.ok(Map.of());
        }

        // Else return users sorted in ascending order by id
        return ResponseEntity.ok(users.stream().sorted().collect(Collectors.toList()));
    }

    public ResponseEntity<?> deleteUser(String email, Authentication auth, HttpServletRequest request) {
        // Find the user by email
        AppUser user = appUserRepository.findByEmailIgnoreCase(email);

        // Check if user was found
        if (user == null) {
            throw new ExceptionWithNotFound("User not found!");
        }

        // Check if user is an administrator
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMINISTRATOR"));

        // If user is an administrator throw exception as admin cannot be deleted
        if (isAdmin) {
            throw new ExceptionWithBadRequest("Can't remove ADMINISTRATOR role!");
        }

        // After passing check delete user from repository
        appUserRepository.delete(user);

        // Prepare response body
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("user", email);
        body.put("status", "Deleted successfully!");

        // Log DELETE_USER event
        String subject = auth.getName();
        String path = request.getRequestURI();

        publisher.publishEvent("DELETE_USER", subject, email, path);

        return ResponseEntity.ok(body);
    }

    public ResponseEntity<?> changeRole(RoleChangeDTO body, Authentication auth, HttpServletRequest request) {
        // Find the user by email
        AppUser user = appUserRepository.findByEmailIgnoreCase(body.getUser());
        if (user == null) {
            throw new ExceptionWithNotFound("User not found!");
        }

        // Validation steps
        // Step 1: Check if the requested role exists in the repository
        Role roleToChange = roleRepository.findByName(body.getRole());
        if (roleToChange == null) {
            throw new ExceptionWithNotFound("Role not found!");
        }

        // Fetch all roles assigned to user
        Set<Role> userRoles = user.getRoles();

        // Steps exclusive to REMOVE operation
        if (body.getOperation().equals("REMOVE")) {

            // Step 2: Check if the user currently has the role being modified
            if (!userRoles.contains(roleToChange)) {
                throw new ExceptionWithBadRequest("The user does not have a role!");
            }

            // Step 3: Prevent removing the ADMINISTRATOR role
            if (roleToChange.getName().equals("ROLE_ADMINISTRATOR")) {
                throw new ExceptionWithBadRequest("Can't remove ADMINISTRATOR role!");
            }

            // Step 4: Prevent removing the last remaining role
            if (userRoles.size() == 1) {
                throw new ExceptionWithBadRequest("The user must have at least one role!");
            }
        }

        // Step 5: Check for conflicting role assignments
        Set<Role> adminRoles = Set.of(new Role("ROLE_ADMINISTRATOR"));
        Set<Role> businessRoles = Set.of(new Role("ROLE_ACCOUNTANT"), new Role("ROLE_USER"), new Role("ROLE_AUDITOR"));

        boolean isRoleAdmin = userRoles.stream().anyMatch(adminRoles::contains);
        boolean isRoleBusiness = userRoles.stream().anyMatch(businessRoles::contains);
        boolean isNewRoleAdmin = roleToChange.getName().equals("ROLE_ADMINISTRATOR");
        boolean isNewRoleBusiness = businessRoles.contains(roleToChange);

        if ((isRoleAdmin && isNewRoleBusiness) || (isRoleBusiness && isNewRoleAdmin)) {
            throw new ExceptionWithBadRequest("The user cannot combine administrative and business roles!");
        }
        // Verification finished

        // Prepare variables for logging
        String action;
        String subject = auth.getName();
        String role = roleToChange.getName().substring(5);
        String object;
        String path = request.getRequestURI();

        // Grant or remove the role based on the operation
        if (body.getOperation().equals("GRANT")) {
            userRoles.add(roleToChange);

            // Log GRANT_ROLE event
            action = "GRANT_ROLE";
            object = "Grant role " + role + " to " + user.getEmail();

        } else if (body.getOperation().equals("REMOVE")) {
            userRoles.remove(roleToChange);

            // Log REMOVE_ROLE event
            action = "REMOVE_ROLE";
            object = "Remove role " + role + " from " + user.getEmail();
        } else {
            throw new ExceptionWithBadRequest("Invalid operation! Must be either GRANT or REMOVE.");
        }

        // Save the user with updated roles
        appUserRepository.save(user);

        publisher.publishEvent(action, subject, object, path);

        return ResponseEntity.ok(user);
    }

    @Transactional
    public ResponseEntity<?> changeUserBlockedStatus(UserBlockedStatusChangeDTO body, Authentication auth, HttpServletRequest request) {
        // Find the user from body by email
        AppUser user = appUserRepository.findByEmailIgnoreCase(body.getUser());

        // Check if user from body was found
        if (user == null) {
            throw new ExceptionWithNotFound("User not found!");
        }

        // Check if user from body is an admin
        if (user.getRoles().contains(new Role("ROLE_ADMINISTRATOR"))) {
            throw new ExceptionWithBadRequest("Can't lock the ADMINISTRATOR!");
        }

        // Check if operations in body are correct
        if (!body.getOperation().matches("LOCK|UNLOCK")) {
            throw new ExceptionWithBadRequest("Invalid operation! Must be either LOCK or UNLOCK.");
        }

        // Get user blocked status and operation type from body
        boolean isUserBlocked = user.isBlocked();
        boolean isBodyRequestingBlocking = body.getOperation().equals("LOCK");

        // Check if operation is possible
        if (isUserBlocked && isBodyRequestingBlocking) {
            throw new ExceptionWithBadRequest("Invalid operation! User is already blocked.");
        } else if (!isUserBlocked && !isBodyRequestingBlocking) {
            throw new ExceptionWithBadRequest("Invalid operation! User is not blocked.");
        }

        // Prepare variables for logging
        String action;
        String userEmail = user.getEmail();
        String subject;
        String object;
        String path = request.getRequestURI();

        // Perform requested operation
        if (isBodyRequestingBlocking) {
            user.setBlocked(true);
            action = "LOCK_USER";
            subject = userEmail;
            object = "Lock user " + userEmail;
        } else {
            user.setBlocked(false);
            action = "UNLOCK_USER";
            subject = auth.getName();
            object = "Unlock user " + user.getEmail();
            attemptRepository.deleteByEmail(userEmail);
        }
        appUserRepository.save(user);

        // Log LOCK_USER or UNLOCK_USER event
        publisher.publishEvent(action, subject, object, path);

        // Return response body
        String performedOperation = isBodyRequestingBlocking ? "locked" : "unlocked";
        String status = "User " + userEmail + " " + performedOperation + "!";
        return ResponseEntity.ok(Map.of("status", status));
    }
}
