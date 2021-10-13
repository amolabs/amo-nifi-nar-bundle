package com.pentasecurity.core.dto.market;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginRequest {
    private String userId;
    private String password;
}
