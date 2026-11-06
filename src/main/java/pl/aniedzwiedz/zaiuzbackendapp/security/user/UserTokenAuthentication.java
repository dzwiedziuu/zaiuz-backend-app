package pl.aniedzwiedz.zaiuzbackendapp.security.user;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

class UserTokenAuthentication extends AbstractAuthenticationToken {

	public static final String ROLE_ADMIN = "ROLE_ADMIN";

	private final UserPrincipal principal;

	UserTokenAuthentication(UserPrincipal principal) {
		super(List.of(new SimpleGrantedAuthority(ROLE_ADMIN)));
		this.principal = principal;
		setAuthenticated(true);
	}

	@Override
	public UserPrincipal getPrincipal() {
		return this.principal;
	}

	@Override
	public Object getCredentials() {
		return null;
	}
}
