package pl.aniedzwiedz.zaiuzbackendapp.parser;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Component
public class TimestampParser {

	private static final Instant MIN_TIME = Instant.parse("0001-01-01T00:00:00Z");
	private static final Instant MAX_TIME = Instant.parse("9999-12-31T23:59:59Z");
	private static final String DATE_REQUIRED = " musi być datą ISO 8601 ze strefą czasową z lat 1-9999";

	public Instant parseOrNow(String text, String field) {
		return (text != null ? parse(text, field) : Instant.now()).truncatedTo(ChronoUnit.MICROS);
	}

	Instant parse(String text, String field) {
		try {
			return supported(OffsetDateTime.parse(text).toInstant(), HttpStatus.UNPROCESSABLE_CONTENT, field);
		}
		catch (DateTimeParseException ex) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, field + DATE_REQUIRED);
		}
	}

	public Instant supported(OffsetDateTime time, String field) {
		return supported(time.toInstant(), HttpStatus.BAD_REQUEST, field);
	}

	private static Instant supported(Instant time, HttpStatus status, String field) {
		if (time.isBefore(MIN_TIME) || time.isAfter(MAX_TIME)) {
			throw new ResponseStatusException(status, field + DATE_REQUIRED);
		}
		return time;
	}
}
