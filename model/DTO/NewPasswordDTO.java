package account.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NewPasswordDTO {
    @JsonProperty("new_password")
    String newPassword;
}