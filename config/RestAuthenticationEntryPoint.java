package account.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Authentication entry point for handling unauthorized access attempts in REST APIs.
 * This class responds with an HTTP 401 Unauthorized status when a request lacks valid
 * authentication (e.g., missing, incorrect, or expired credentials).
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Called when a user tries to access a secured REST endpoint without proper authentication.
     * Responds with HTTP 401 Unauthorized and includes the exception message.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authException the exception indicating the authentication error
     * @throws IOException if an I/O error occurs while sending the error response
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
