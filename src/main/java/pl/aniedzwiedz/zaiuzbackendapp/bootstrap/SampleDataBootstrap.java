package pl.aniedzwiedz.zaiuzbackendapp.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.aniedzwiedz.zaiuzbackendapp.model.Measurement;
import pl.aniedzwiedz.zaiuzbackendapp.model.Series;
import pl.aniedzwiedz.zaiuzbackendapp.repository.MeasurementRepository;
import pl.aniedzwiedz.zaiuzbackendapp.repository.SeriesRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class SampleDataBootstrap implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SampleDataBootstrap.class);

	private static final int MEASUREMENTS_PER_SERIES = 24;

	private record Sample(Series series, double base, double amplitude, double noise) {
	}

	private final SeriesRepository series;
	private final MeasurementRepository measurements;

	SampleDataBootstrap(SeriesRepository series, MeasurementRepository measurements) {
		this.series = series;
		this.measurements = measurements;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		List<Sample> samples = List.of(
				new Sample(Series.builder().name("Temperatura").minValue(-30.0).maxValue(50.0).color("#E4572E").icon("thermometer").unit("°C").build(), 18, 6, 0.8),
				new Sample(Series.builder().name("Wilgotność").minValue(0.0).maxValue(100.0).color("#17BEBB").icon("droplet").unit("%").build(), 55, 15, 3),
				new Sample(Series.builder().name("Ciśnienie").minValue(950.0).maxValue(1050.0).color("#4C6EF5").icon("gauge").unit("hPa").build(), 1013, 4, 1));
		Random random = new Random(42);
		Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
		Set<String> existing = this.series.findAll().stream().map(Series::getName).collect(Collectors.toSet());
		for (Sample sample : samples) {
			if (existing.contains(sample.series().getName())) {
				continue;
			}
			Series saved = this.series.save(sample.series());
			List<Measurement> values = new ArrayList<>();
			for (int i = MEASUREMENTS_PER_SERIES; i > 0; i--) {
				Instant at = now.minus(Duration.ofHours(i));
				double daily = Math.sin(2 * Math.PI * (i % 24) / 24.0);
				double value = sample.base() + sample.amplitude() * daily + sample.noise() * random.nextGaussian();
				values.add(Measurement.builder()
						.seriesId(saved.getId())
						.series(saved)
						.value(clamp(round(value), saved))
						.measuredAt(at)
						.build());
			}
			this.measurements.saveAll(values);
			log.info("Utworzono przykładową serię '{}' z {} wynikami", saved.getName(), values.size());
		}
	}

	private static double round(double value) {
		return Math.round(value * 10) / 10.0;
	}

	private static double clamp(double value, Series series) {
		return Math.max(series.getMinValue(), Math.min(series.getMaxValue(), value));
	}
}
