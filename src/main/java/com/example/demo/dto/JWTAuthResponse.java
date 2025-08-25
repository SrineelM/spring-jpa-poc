package com.example.demo.dto;

/**
 * A Data Transfer Object (DTO) that represents the response sent to the client
 * after a successful authentication.
 * It contains the JSON Web Token (JWT) that the client needs to use for
 * authenticating subsequent requests.
 */
public class JWTAuthResponse {

    /**
     * The JWT access token.
     */
    private String accessToken;

    /**
     * The type of the token, which is typically "Bearer".
     */
    private String tokenType = "Bearer";

    public JWTAuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    // Getters and Setters

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
