package com.project.dscatalog.dto;

import com.project.dscatalog.services.validation.UserInsertValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@UserInsertValid
public class UserInsertDTO extends UserDTO {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Campo obrigatório")
    @Size(min = 8, message = "Deve ter no mínimo 8 caracteres")
    private String password;

    UserInsertDTO() {
        super();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }
}
