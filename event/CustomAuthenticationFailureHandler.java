package account.event;

import account.model.AppUser;
import account.model.LoginAttempt;
import account.repository.AppUserRepository;
import account.repository.LoginAttemptRepository;
import  jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static account.event.util.EventUtil.*;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserEventPublisher publisher;
    private final LoginAttemptRepository attemptRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // Prepare HttpServletResponse body
        // Get URI from HttpServletRequest
        String path = (String) request.getAttribute("originalRequestURI");

        // Set status code and JSON content type to HttpServletResponse
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", "User account is locked");
        body.put("path", path);

        mapAndSendResponse(body, response);

        // Log LOGIN_FAILED event
        // Get email form Authorization header
        String authHeader = request.getHeader("Authorization");
        String email = getEmailFromAuthHeader(authHeader);

        // Don't record null logins
        if (email == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
        }

        // Don't record blocked user logins
        if (email.matches(".+@acme.com") && appUserRepository.findByEmailIgnoreCase(email).isBlocked()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User account is blocked");
            return;
        }

        publisher.publishEvent("LOGIN_FAILED", email, path, path);

        // Save failed attempt
        attemptRepository.save(new LoginAttempt(email, false));

        // Prevent brute-force attack
        // Count failed attempts
        long failedAttempts = attemptRepository.countByEmailAndSuccessFalse(email);

        // If failed attempts exceed threshold, block user
        if (failedAttempts > 5) {
            AppUser user = appUserRepository.findByEmailIgnoreCase(email);
            if (user != null) {
                // Block user
                user.setBlocked(true);
                appUserRepository.save(user);

                // Publish brute force and lock user events
                publisher.publishEvent("BRUTE_FORCE", email, path, path);

                // Prevent locking admin account
                if (user.getRoleNames().contains("ROLE_ADMINISTRATOR")) {
                    throw new BadCredentialsException("Can't lock the ADMINISTRATOR!");
                }

                publisher.publishEvent("LOCK_USER", email, "Lock user " + email, path);

                // Provide a specific message when blocking the user
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User blocked due to too many failed login attempts");
                return;
            }
        }

        // If failed attempts didn't exceed threshold provide a specific message
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
    }
}
