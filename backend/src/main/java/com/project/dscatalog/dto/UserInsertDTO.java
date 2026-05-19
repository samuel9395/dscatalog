package com.project.dscatalog.dto;

public class UserInsertDTO extends UserDTO {
    private static final long serialVersionUID = 1L;

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
