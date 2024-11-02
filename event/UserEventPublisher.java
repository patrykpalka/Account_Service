package account.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishEvent(String action, String subject, String object, String path) {
        LocalDateTime date = LocalDateTime.now();
        publisher.publishEvent(new UserActionEvent(this, date, action, subject, object, path));
    }
}
