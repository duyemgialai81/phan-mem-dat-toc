package com.example.demo.core.login.modal.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserRquest {
    private String username;
    private String password;

}
