package account.event;

import account.repository.LogsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final LogsRepository logsRepository;

    // When event is published save log in repository
    @EventListener
    public void onUserAction(UserActionEvent event) {
        logsRepository.save(event.getLog());
    }
}
