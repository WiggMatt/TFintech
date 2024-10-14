package ru.matthew.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.matthew.dto.EventDTO;
import ru.matthew.dto.EventResponseDTO;
import ru.matthew.exception.ReceivingEventsException;
import ru.matthew.service.external.KudaGoApiService;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EventServiceCompletableFuture {
    private final KudaGoApiService kudaGoApiService;
    private final CurrencyService currencyService;
    private final DateService dateService;

    @Autowired
    public EventServiceCompletableFuture(KudaGoApiService kudaGoApiService,
                                         CurrencyService currencyService,
                                         DateService dateService) {
        this.kudaGoApiService = kudaGoApiService;
        this.currencyService = currencyService;
        this.dateService = dateService;
    }

    public CompletableFuture<EventResponseDTO> fetchEvents(double budget, String currency, String dateFrom, String dateTo) {
        log.info("Запрос событий с бюджетом: {} {}, с датами от {} до {}", budget, currency, dateFrom, dateTo);

        LocalDate[] dates = dateService.determineDates(dateFrom, dateTo);
        LocalDate fromDate = dates[0];
        LocalDate toDate = dates[1];

        CompletableFuture<List<EventDTO>> eventsFuture = kudaGoApiService.fetchEventsAsync(fromDate, toDate);
        CompletableFuture<Double> budgetFuture = CompletableFuture.supplyAsync(() -> currencyService.convertBudgetForCompletableFuture(budget, currency));

        return eventsFuture
                .thenCombine(budgetFuture, (events, budgetInRUB) -> {
                    log.info("Получено {} событий, фильтрация по бюджету: {} RUB", events.size(), budgetInRUB);
                    List<EventDTO> suitableEvents = filterEventsByBudget(events, budgetInRUB);
                    return new EventResponseDTO(suitableEvents.size(), suitableEvents);
                })
                .exceptionally(e -> {
                    log.error("Ошибка при получении событий: {}", e.getMessage(), e);
                    throw new ReceivingEventsException("Ошибка при получении событий");
                });
    }

    private List<EventDTO> filterEventsByBudget(List<EventDTO> events, double budgetInRUB) {
        List<EventDTO> suitableEvents = events.stream()
                .filter(event -> parsePrice(event.getPrice()) <= budgetInRUB)
                .toList();
        log.info("Найдено подходящих событий: {}", suitableEvents.size());
        return suitableEvents;
    }

    private double parsePrice(String priceString) {
        try {
            String numericString = priceString.replaceAll("[^\\d.]", "");
            double price = Double.parseDouble(numericString);
            log.debug("Парсинг цены из строки '{}': {}", priceString, price);
            return price;
        } catch (NumberFormatException e) {
            log.error("Ошибка при парсинге цены: {}", priceString, e);
            return Double.MAX_VALUE;
        }
    }
}
