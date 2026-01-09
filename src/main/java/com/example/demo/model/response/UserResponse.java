package com.example.demo.model.response;

import com.example.demo.entity.User;
import lombok.Data;

@Data
public class UserResponse {
    public Integer id;
    private String name;
    private String password;
    public UserResponse(User user){
        this.id = user.getId();
        this.name = user.getUsername();
        this.password = user.getPassword();
    }

}
