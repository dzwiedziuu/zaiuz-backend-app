package pl.aniedzwiedz.zaiuzbackendapp.controller;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.aniedzwiedz.zaiuzbackendapp.model.Sensor;
import pl.aniedzwiedz.zaiuzbackendapp.service.SensorService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
class SensorController {

	record SensorCreated(@JsonUnwrapped Sensor sensor, String apiKey) {
	}

	private final SensorService service;

	SensorController(SensorService service) {
		this.service = service;
	}

	@GetMapping
	List<Sensor> list() {
		return this.service.list();
	}

	@GetMapping("/{id}")
	Sensor get(@PathVariable long id) {
		return this.service.get(id);
	}

	@PostMapping
	ResponseEntity<SensorCreated> register(@RequestBody Sensor input) {
		String apiKey = this.service.create(input);
		return ResponseEntity.created(URI.create("/api/sensors/" + input.getId()))
			.body(new SensorCreated(input, apiKey));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deregister(@PathVariable long id) {
		this.service.delete(id);
	}
}
