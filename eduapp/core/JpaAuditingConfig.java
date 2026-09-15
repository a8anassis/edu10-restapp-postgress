package gr.aueb.cf.eduapp.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Kept out of {@code EduAppApplication} on purpose: {@code @WebMvcTest} always processes the
 * {@code @SpringBootApplication} class's own annotations regardless of slice, so
 * {@code @EnableJpaAuditing} there would pull in JPA auditing beans (which need a real
 * {@code EntityManagerFactory}) into web-only test contexts. A plain {@code @Configuration}
 * class is excluded by the web slice's component scan instead.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
