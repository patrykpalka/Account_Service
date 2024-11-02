package account.event;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static account.event.util.EventUtil.*;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final UserEventPublisher publisher;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // Prepare HttpServletResponse body
        // Get URI from HttpServletRequest
        String path = request.getRequestURI();

        // Set status code and JSON content type to HttpServletResponse
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpServletResponse.SC_FORBIDDEN);
        body.put("error", "Forbidden");
        body.put("message", "Access Denied!");
        body.put("path", path);

        mapAndSendResponse(body, response);

        // Log ACCESS_DENIED event
        // Get email form Authorization header
        String authHeader = request.getHeader("Authorization");
        String email = getEmailFromAuthHeader(authHeader);

        // Don't record null logins
        if (email == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
        }

        publisher.publishEvent("ACCESS_DENIED", email, path, path);
    }
}
