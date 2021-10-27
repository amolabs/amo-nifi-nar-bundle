package com.pentasecurity.core.dto.market;

import lombok.Getter;

@Getter
public class ParcelErrorResponse {
    private String message;
    private int status;
    private boolean success;
}
