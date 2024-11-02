package account.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to capture and store the original request URI for each HTTP request.
 * This URI remains accessible even after internal forwarding or redirection
 * (e.g., in custom error handling, where the URI might change to /error).
 */
@Component
public class OriginalRequestFilter extends OncePerRequestFilter {

    /**
     * Stores the original request URI as an attribute ("originalRequestURI")
     * to keep it available throughout the request lifecycle.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Capture the original URI before any redirection or internal forwarding
        request.setAttribute("originalRequestURI", request.getRequestURI());

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
