package pl.aniedzwiedz.zaiuzbackendapp.controller.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.aniedzwiedz.zaiuzbackendapp.security.user.UserPrincipal;
import pl.aniedzwiedz.zaiuzbackendapp.service.auth.AuthService;
import pl.aniedzwiedz.zaiuzbackendapp.service.auth.LoginRequest;
import pl.aniedzwiedz.zaiuzbackendapp.service.auth.LoginResponse;
import pl.aniedzwiedz.zaiuzbackendapp.service.auth.PasswordChange;

@RestController
@RequestMapping("/api/auth")
class AuthController {

	private final AuthService auth;

	AuthController(AuthService auth) {
		this.auth = auth;
	}

	@PostMapping("/login")
	LoginResponse login(@RequestBody LoginRequest request) {
		if (request.username() == null || request.password() == null) {
			// customowa walidacja aby zwrocic 400
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pola username i password są wymagane");
		}
		return this.auth.login(request.username(), request.password());
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void logout(@AuthenticationPrincipal UserPrincipal principal) {
		this.auth.logout(principal);
	}

	@PutMapping("/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void changePassword(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PasswordChange request) {
		this.auth.changePassword(principal, request.currentPassword(), request.newPassword());
	}
}
