package com.pentasecurity.core.dto.market;

import lombok.Getter;

@Getter
public class JwtLoginPayload {
    private long sellerId;
    private long buyerId;
    private long memberId;
}
