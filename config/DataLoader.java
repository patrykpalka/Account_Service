package account.config;

import account.model.Role;
import account.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader {

    private final RoleRepository roleRepository;

    // Populate Role table with available roles at the start of application
    @PostConstruct
    public void init() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("ROLE_USER"));
            roleRepository.save(new Role("ROLE_ADMINISTRATOR"));
            roleRepository.save(new Role("ROLE_ACCOUNTANT"));
            roleRepository.save(new Role("ROLE_AUDITOR"));
        }
    }
}
