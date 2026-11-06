package pl.aniedzwiedz.zaiuzbackendapp.service.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PasswordChange(@NotNull String currentPassword, @NotNull @Size(min = 8) String newPassword) {
}
