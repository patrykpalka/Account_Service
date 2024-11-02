package account.service.util;

import account.model.DTO.PasswordListDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class BreachedPasswordChecker {

    private static final List<String> passwordList;

    // Static block to load password list from JSON file using InputStream
    static {
        try {
            // Load the passwords.json from the resources folder using InputStream
            Resource resource = new ClassPathResource("passwords.json");
            InputStream inputStream = resource.getInputStream();

            // Use ObjectMapper to map InputStream to PasswordListDTO class
            passwordList = new ObjectMapper().readValue(inputStream, PasswordListDTO.class).getPasswords();

            // Close InputStream
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load breached passwords list");
        }
    }

    // Method to check if the password is in the breached list
    public static boolean isPasswordBreached(String newPassword) {
        return passwordList.contains(newPassword);
    }
}