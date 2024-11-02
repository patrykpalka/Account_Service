package account.repository;

import account.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    long countByEmailAndSuccessFalse(String email);
    void deleteByEmail(String email);
}
