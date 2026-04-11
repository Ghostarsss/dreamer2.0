package com.dreamer.authservice.entity.dto;

import lombok.Data;

@Data
public class LoginDto {

    private String email;

    private String password;
}
