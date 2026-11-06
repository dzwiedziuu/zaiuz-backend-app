package pl.aniedzwiedz.zaiuzbackendapp.model.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(indexes = { @Index(columnList = "userId"), @Index(columnList = "expiresAt") })
@Getter
@NoArgsConstructor
public class Session {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Setter
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter(AccessLevel.NONE)
	private User user;

	@NotNull
	@Column(unique = true)
	@Getter(AccessLevel.NONE)
	@Setter
	private String tokenHash;

	@NotNull
	@Setter
	private Instant expiresAt;
}
