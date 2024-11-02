package account.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleChangeDTO {

    @NotBlank
    private String user;

    @NotBlank
    private String role;

    @NotBlank
    private String operation;

    public void setRole(String role) {
        this.role = "ROLE_" + role;
    }
}
