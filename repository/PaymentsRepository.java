package account.repository;

import account.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {
    List<Payments> findAllByEmployee(String employee);
    List<Payments> findAllByEmployeeInAndPeriodIn(List<String> employees, List<String> periods);
    Payments findByEmployeeAndPeriod(String employee, String period);
}