package com.example.demo.core.login.modal.response;

import com.example.demo.entity.User;
import lombok.Data;

@Data
public class UserResponse {
    public String username;
    public String password;
    public String fullName;
    public String phone;
    public UserResponse(User user){
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
    }
}
