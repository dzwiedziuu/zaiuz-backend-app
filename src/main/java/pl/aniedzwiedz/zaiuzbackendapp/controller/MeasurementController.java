package pl.aniedzwiedz.zaiuzbackendapp.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.aniedzwiedz.zaiuzbackendapp.model.Measurement;
import pl.aniedzwiedz.zaiuzbackendapp.parser.TimestampParser;
import pl.aniedzwiedz.zaiuzbackendapp.security.sensor.SensorPrincipal;
import pl.aniedzwiedz.zaiuzbackendapp.service.measurement.MeasurementSearchInput;
import pl.aniedzwiedz.zaiuzbackendapp.service.measurement.MeasurementService;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/measurements")
class MeasurementController {

	private final MeasurementService service;

	private final TimestampParser timestamps;

	MeasurementController(MeasurementService service, TimestampParser timestamps) {
		this.service = service;
		this.timestamps = timestamps;
	}

	@GetMapping
	List<Measurement> search(@RequestParam(required = false) String series,
							 @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime from,
							 @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime to,
							 @RequestParam(required = false) String sort, @RequestParam(required = false) Integer limit) {
		return this.service.search(new MeasurementSearchInput(series, from, to, sort, limit, this.timestamps));
	}

	@GetMapping("/{id}")
	Measurement get(@PathVariable long id) {
		return this.service.get(id);
	}

	@PostMapping
	ResponseEntity<Measurement> create(@AuthenticationPrincipal SensorPrincipal sensor,
			@RequestBody Measurement input) {
		Measurement created = this.service.record(sensor, input);
		return ResponseEntity.created(URI.create("/api/measurements/" + created.getId())).body(created);
	}
}
