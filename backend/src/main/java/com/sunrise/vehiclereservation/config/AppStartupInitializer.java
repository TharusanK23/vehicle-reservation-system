package com.sunrise.vehiclereservation.config;

import com.sunrise.vehiclereservation.pattern.singleton.AppConfigManager;
import com.sunrise.vehiclereservation.pattern.singleton.ReservationNumberGenerator;
import com.sunrise.vehiclereservation.repository.ReservationRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Bridges Spring's dependency-injected configuration into the two plain-Java
 * Singletons ({@link AppConfigManager}, {@link ReservationNumberGenerator}) exactly
 * once at boot, so that the reservation-number sequence continues correctly across
 * application restarts instead of resetting to zero.
 */
@Component
public class AppStartupInitializer implements ApplicationRunner {

    private final ReservationRepository reservationRepository;
    private final BusinessProperties businessProperties;

    public AppStartupInitializer(ReservationRepository reservationRepository, BusinessProperties businessProperties) {
        this.reservationRepository = reservationRepository;
        this.businessProperties = businessProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        long currentCount = reservationRepository.count();
        ReservationNumberGenerator.getInstance().initialise(currentCount);

        AppConfigManager config = AppConfigManager.getInstance();
        config.set("currencySymbol", businessProperties.getCurrencySymbol());
        config.set("taxRate", String.valueOf(businessProperties.getTaxRate()));
        config.set("systemName", "Sunrise Vehicle Rentals - Online Vehicle Reservation System");
    }
}
