package account.repository;

import account.model.DTO.LogDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogsRepository extends JpaRepository<LogDTO, Long> {
}
