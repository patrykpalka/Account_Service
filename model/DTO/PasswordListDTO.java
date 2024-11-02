package account.model.DTO;

import lombok.Data;

import java.util.List;

@Data
public class PasswordListDTO {
    private List<String> passwords;
}