package com.example.demo.core.login.modal.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String phone;
}
