package account.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.h2.util.StringUtils;
import org.hibernate.annotations.SortNatural;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "name", "lastname", "email"})
public class AppUser implements Comparable<AppUser> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String name;

    @NotBlank
    private String lastname;

    @NotBlank
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    private String password;

    @JsonIgnore
    private boolean blocked;

    // Join with role table using many-to-many relationship
    // User can have multiple roles and a role can be assigned to multiple users
    // Set fetching strategy to make roles be loaded immediately when the user entity is loaded
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )

    private Set<Role> roles = new HashSet<>();

    // This method will be invoked instead of getRoles when serializing to JSON
    // It ensures that only names of role are visible in response body
    // When getRole is called in code it will return Set of roles not String
    @JsonProperty("roles")
    public Set<String> getRoleNames() {
        return roles.stream()
                .sorted()
                .map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Ensure that emails are always in lowercase
    public void setEmail(String emailValue) {
        email = StringUtils.toLowerEnglish(emailValue);
    }

    // Allow comparison in ascending order
    @Override
    public int compareTo(AppUser other) {
        return Long.compare(this.id, other.id);
    }
}