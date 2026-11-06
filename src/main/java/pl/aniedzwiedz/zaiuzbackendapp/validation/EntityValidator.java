package pl.aniedzwiedz.zaiuzbackendapp.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EntityValidator {

	private final Validator validator;

	EntityValidator(Validator validator) {
		this.validator = validator;
	}

	public <T> T validate(T entity) {
		Set<ConstraintViolation<T>> violations = this.validator.validate(entity);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		return entity;
	}
}
