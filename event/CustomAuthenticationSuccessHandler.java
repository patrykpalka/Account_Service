package account.event;

import account.repository.LoginAttemptRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginAttemptRepository attemptRepository;

    @Autowired
    public CustomAuthenticationSuccessHandler(LoginAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // If authentication succeeds clear consecutive failed attempts counter
        String email = request.getParameter("email");
        attemptRepository.deleteByEmail(email);
    }
}
