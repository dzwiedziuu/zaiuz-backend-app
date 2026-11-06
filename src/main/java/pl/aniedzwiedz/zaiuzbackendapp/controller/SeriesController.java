package pl.aniedzwiedz.zaiuzbackendapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.aniedzwiedz.zaiuzbackendapp.model.Series;
import pl.aniedzwiedz.zaiuzbackendapp.service.SeriesService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/series")
class SeriesController {

	private final SeriesService service;

	SeriesController(SeriesService service) {
		this.service = service;
	}

	@GetMapping
	List<Series> list() {
		return this.service.list();
	}

	@GetMapping("/{id}")
	Series get(@PathVariable long id) {
		return this.service.get(id);
	}

	@PostMapping
	ResponseEntity<Series> create(@RequestBody Series input) {
		Series created = this.service.create(input);
		return ResponseEntity.created(URI.create("/api/series/" + created.getId())).body(created);
	}

	@PutMapping("/{id}")
	Series update(@PathVariable long id, @RequestBody Series input) {
		return this.service.update(id, input);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@PathVariable long id) {
		this.service.delete(id);
	}
}
