package org.spring.MySite.DTO;


import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    @NotEmpty(message="Username should not be empty")
    @Size(min=2, max=30, message = "Username should be between 2 and 30 characters")
    private String username;

    @NotEmpty(message = "Password should not be empty")
    @Size(min=4, max=10, message = "Password should be between 4 and 10 characters")
    private String password;

    @NotEmpty(message = "Email should not be empty")
    @Size(min=0, max=70, message = "Email should be less then 70 characters")
    @Email(message = "Email should be valid")
    private String email;

    @NotEmpty(message = "Field should not be empty")
    @Transient
    private String passwordReg;
}
