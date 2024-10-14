package ru.matthew.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.matthew.exception.CurrencyNotFoundException;
import ru.matthew.exception.ServiceUnavailableException;
import ru.matthew.service.external.CurrencyRateClient;

@Service
@Slf4j
public class CurrencyService {

    private final CurrencyRateClient currencyRateClient;

    public CurrencyService(CurrencyRateClient currencyRateClient) {
        this.currencyRateClient = currencyRateClient;
    }

    public Mono<Double> convertBudget(double budget, String currency) {
        return currencyRateClient.convertCurrency(currency, "RUB", budget)
                .onErrorMap(CurrencyNotFoundException.class, ex -> new CurrencyNotFoundException("Валюта не найдена: " + currency))
                .onErrorMap(ex -> new ServiceUnavailableException("Ошибка при взаимодействии с внешним сервисом", 3600));
    }

    public Double convertBudgetForCompletableFuture(double budget, String currency) {
        log.debug("Конвертация бюджета {} {} в RUB", budget, currency);
        return currencyRateClient.convertCurrency2(currency, "RUB", budget);
    }
}
