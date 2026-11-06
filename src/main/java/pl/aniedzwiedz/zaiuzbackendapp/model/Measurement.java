package pl.aniedzwiedz.zaiuzbackendapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(indexes = {
		@Index(columnList = "seriesId, measuredAt"),
		@Index(columnList = "measuredAt"),
		@Index(columnList = "sensorId")
})
@Getter
@NoArgsConstructor
public class Measurement {

	private static final Duration MAX_CLOCK_SKEW = Duration.ofMinutes(5);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Long id;

	@NotNull
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter
	private Long seriesId;

	@ManyToOne
	@JoinColumn(name = "seriesId", insertable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	@Setter
	private Series series;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Setter
	private Long sensorId;

	@ManyToOne
	@JoinColumn(name = "sensorId", insertable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JsonIgnore
	private Sensor sensor;

	@NotNull
	private Double value;

	@NotNull
	@JsonProperty("timestamp")
	@Setter
	private Instant measuredAt;

	@Transient
	@Getter
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String requestedTimestamp;

	@JsonSetter("timestamp")
	void setTimestamp(String timestamp) {
		this.requestedTimestamp = timestamp;
	}

	@AssertTrue(message = "Pole timestamp nie może być późniejsze niż 5 minut od bieżącego czasu serwera")
	@JsonIgnore
	boolean isTimestampAllowed() {
		return this.measuredAt == null || !this.measuredAt.isAfter(Instant.now().plus(MAX_CLOCK_SKEW));
	}

	@AssertTrue(message = "Wartość musi mieścić się w przedziale serii")
	@JsonIgnore
	boolean isValueInRange() {
		return this.value == null || this.series == null || this.value >= this.series.getMinValue() && this.value <= this.series.getMaxValue();
	}
}
