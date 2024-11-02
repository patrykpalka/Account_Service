package account.event;

import account.model.DTO.LogDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserActionEvent extends ApplicationEvent {

    private LocalDateTime date;
    private String action;
    private String subject;
    private String object;
    private String path;

    public UserActionEvent(Object source, LocalDateTime date, String action, String subject, String object, String path) {
        super(source);
        this.date = date;
        this.action = action;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }

    public LogDTO getLog() {
        return new LogDTO(date, action, subject, object, path);
    }
}
