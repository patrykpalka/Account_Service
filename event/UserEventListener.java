package account.event;

import account.repository.LogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final LogsRepository logsRepository;

    @Autowired
    public UserEventListener(LogsRepository logsRepository) {
        this.logsRepository = logsRepository;
    }

    // When event is published save log in repository
    @EventListener
    public void onUserAction(UserActionEvent event) {
        logsRepository.save(event.getLog());
    }
}
