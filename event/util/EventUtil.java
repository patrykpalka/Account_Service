package account.event.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class EventUtil {

    public static void mapAndSendResponse(Map<String, Object> body, HttpServletResponse response) throws IOException {
        // Convert Map to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = mapper.writeValueAsString(body);

        // Send JSON to HttpServletResponse
        response.getWriter().write(jsonResponse);
        response.getWriter().flush(); // Ensure response is flushed
    }

    public static String getEmailFromAuthHeader(String authHeader) {
        // Check if the Authorization header is present and starts with "Basic "
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            // Extract the Base64 encoded string and decode it
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

            // Split the credentials into username and password
            String[] values = credentials.split(":", 2); // Split into two parts
            return values[0]; // First part is the email (username)
        }

        // Else return null
        return null;
    }
}
