package com.example.docmgmt.model;

import lombok.Data;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class JwtRequest {
    private String username;
    private String password;
}