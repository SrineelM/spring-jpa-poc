package com.example.demo.dto;

/**
 * Authentication response DTO returned after a successful login.
 * Carries the access token (JWT) the client must send in the Authorization header
 * for subsequent protected requests.
 */
public class JWTAuthResponse {

    /**
     * The signed JWT access token string produced by the security layer.
     * This token typically encodes the subject (user id / username), authorities,
     * issue & expiration timestamps and is cryptographically signed so the server
     * can trust it without doing a DB lookup on every request.
     */
    private String accessToken;

    /**
     * Token type hint returned to the client. For HTTP Authorization headers the
     * conventional prefix is "Bearer" (i.e. Authorization: Bearer <token>).
     * Exposed so it can be overridden in the future (e.g. for opaque tokens).
     */
    private String tokenType = "Bearer";

    /**
     * Convenience constructor used by controllers/services when issuing a token.
     * @param accessToken freshly generated JWT
     */
    public JWTAuthResponse(String accessToken) {
        this.accessToken = accessToken; // store raw JWT string
    }

    // --- Accessors (kept simple – DTO is a shallow data carrier) ---

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}
