package account.repository;

import account.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
     AppUser findByEmailIgnoreCase(String email);
}