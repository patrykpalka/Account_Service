package account.service;

import account.model.DTO.LogDTO;
import account.repository.LogsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final LogsRepository logsRepository;

    public ResponseEntity<?> getListOfEvents() {
        List<LogDTO> logs = logsRepository.findAll();

        if (logs.isEmpty()) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.ok(logs);
    }
}
