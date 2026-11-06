package pl.aniedzwiedz.zaiuzbackendapp.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	// mapowanie pol technicznych (domniemanych na podstawie refleksji) na pola w API
	private static final Map<String, String> API_FIELDS = Map.of(
			"measuredAt", "timestamp",
			"timestampAllowed", "timestamp",
			"valueInRange", "value",
			"rangeValid", "maxValue");

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, "Dane nie spełniają reguł walidacji");
		List<Map<String, String>> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map((e) -> Map.of("field", e.getField(), "message", message(e)))
			.toList();
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, headers, HttpStatus.UNPROCESSABLE_CONTENT, request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, "Dane nie spełniają reguł walidacji");
		List<Map<String, String>> errors = ex.getConstraintViolations()
			.stream()
			.map((v) -> {
				String field = apiField(leafName(v.getPropertyPath()));
				return Map.of("field", field, "message", message(field, v));
			})
			.toList();
		problem.setProperty("errors", errors);
		return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.UNPROCESSABLE_CONTENT, request);
	}

	private static String message(FieldError error) {
		if (error.contains(ConstraintViolation.class)) {
			return message(error.getField(), error.unwrap(ConstraintViolation.class));
		}
		return String.valueOf(error.getDefaultMessage());
	}

	/** Wspólne komunikaty z nazwą pola; pozostałe reguły (np. @AssertTrue) mają własną treść w adnotacji. */
	private static String message(String field, ConstraintViolation<?> violation) {
		return switch (violation.getConstraintDescriptor().getAnnotation()) {
			case NotNull notNull -> "Pole " + field + " jest wymagane";
			case NotBlank notBlank -> "Pole " + field + " jest wymagane";
			case NotEmpty notEmpty -> "Pole " + field + " jest wymagane";
			case Size size -> "Pole " + field + " " + lengthRule(size);
			case Pattern pattern -> "Pole " + field + " ma niepoprawny format";
			default -> violation.getMessage();
		};
	}

	private static String lengthRule(Size size) {
		if (size.min() == 0) {
			return "może mieć najwyżej " + size.max() + " znaków";
		}
		if (size.max() == Integer.MAX_VALUE) {
			return "musi mieć co najmniej " + size.min() + " znaków";
		}
		return "musi mieć od " + size.min() + " do " + size.max() + " znaków";
	}

	private static String apiField(String name) {
		return API_FIELDS.getOrDefault(name, name);
	}

	private static String leafName(Path path) {
		String name = "";
		for (Path.Node node : path) {
			if (node.getName() != null) {
				name = node.getName();
			}
		}
		return name;
	}

}
