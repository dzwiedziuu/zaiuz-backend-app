package pl.aniedzwiedz.zaiuzbackendapp.security.sensor;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

class ApiKeyAuthentication extends AbstractAuthenticationToken {

	public static final String ROLE_SENSOR = "ROLE_SENSOR";

	private final SensorPrincipal principal;

	ApiKeyAuthentication(SensorPrincipal principal) {
		super(List.of(new SimpleGrantedAuthority(ROLE_SENSOR)));
		this.principal = principal;
		setAuthenticated(true);
	}

	@Override
	public SensorPrincipal getPrincipal() {
		return this.principal;
	}

	@Override
	public Object getCredentials() {
		return null;
	}
}
