package com.ing.brokerage.base.entity;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "jpaAuditorProvider", dateTimeProviderRef = "jpaAuditingDateTimeProvider")
public class JpaAuditConfiguration {


    /**
     * If the user is logged in, return the user's id, otherwise return -1.
     *
     * @return The id of the user who is currently logged in.
     */
    @Bean("jpaAuditorProvider")
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("test");
    }

    /**
     * This function returns the current time in milliseconds.
     *
     * @return Optional.of(OffsetDateTime.now ().truncatedTo(ChronoUnit.MILLIS))
     */
    @Bean("jpaAuditingDateTimeProvider")
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
    }
}
