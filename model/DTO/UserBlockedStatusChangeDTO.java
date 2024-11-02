package account.model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserBlockedStatusChangeDTO {

    @NotBlank
    @Pattern(regexp = ".+@acme.com")
    private String user;

    @NotBlank
    private String operation;
}
