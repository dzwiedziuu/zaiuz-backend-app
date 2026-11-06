package pl.aniedzwiedz.zaiuzbackendapp.service.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
