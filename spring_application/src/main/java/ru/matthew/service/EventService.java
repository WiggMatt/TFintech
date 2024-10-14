package ru.matthew.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.matthew.dto.EventDTO;
import ru.matthew.dto.EventResponseDTO;
import ru.matthew.exception.CurrencyNotFoundException;
import ru.matthew.exception.RateLimitExceededException;
import ru.matthew.exception.ServiceUnavailableException;
import ru.matthew.service.external.KudaGoApiService;

import java.time.LocalDate;

@Service
@Slf4j
public class EventService {

    private final KudaGoApiService kudaGoApiService;
    private final CurrencyService currencyService;
    private final DateService dateService;

    @Autowired
    public EventService(KudaGoApiService kudaGoApiService, CurrencyService currencyService, DateService dateService) {
        this.kudaGoApiService = kudaGoApiService;
        this.currencyService = currencyService;
        this.dateService = dateService;
    }

    public Mono<EventResponseDTO> fetchEvents(double budget, String currency, String dateFrom, String dateTo) {
        LocalDate[] dates = dateService.determineDates(dateFrom, dateTo);
        LocalDate fromDate = dates[0];
        LocalDate toDate = dates[1];

        Flux<EventDTO> eventsFlux = kudaGoApiService.fetchEvents(fromDate, toDate);
        Mono<Double> budgetMono = currencyService.convertBudget(budget, currency);

        return budgetMono.flatMap(budgetInRUB -> eventsFlux
                .filter(event -> parsePrice(event.getPrice()) <= budgetInRUB)
                .collectList()
                .map(suitableEvents -> {
                    log.info("Найдено подходящих событий: {}", suitableEvents.size());
                    return new EventResponseDTO(suitableEvents.size(), suitableEvents);
                })
        ).onErrorMap(this::mapException);
    }

    private double parsePrice(String priceString) {
        try {
            String numericString = priceString.replaceAll("[^\\d.]", "");
            return Double.parseDouble(numericString);
        } catch (NumberFormatException e) {
            log.error("Ошибка при парсинге цены: {}", priceString, e);
            return Double.MAX_VALUE;
        }
    }

    private Throwable mapException(Throwable e) {
        if (e instanceof RateLimitExceededException || e instanceof CurrencyNotFoundException) {
            return e;
        } else {
            return new ServiceUnavailableException("Ошибка при взаимодействии с внешним сервисом", 3600);
        }
    }
}
