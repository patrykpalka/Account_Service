package account.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserEventPublisher {

    private final ApplicationEventPublisher publisher;

    @Autowired
    public UserEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishEvent(String action, String subject, String object, String path) {
        LocalDateTime date = LocalDateTime.now();
        publisher.publishEvent(new UserActionEvent(this, date, action, subject, object, path));
    }
}
