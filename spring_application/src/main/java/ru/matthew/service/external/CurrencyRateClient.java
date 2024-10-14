package ru.matthew.service.external;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.matthew.dto.CurrencyConversionRequest;
import ru.matthew.dto.CurrencyConversionResponse;

@Service
public class CurrencyRateClient {
    private final WebClient webClient;
    private final RestTemplate restTemplate;


    public CurrencyRateClient(WebClient.Builder webClientBuilder, RestTemplate restTemplate) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
        this.restTemplate = restTemplate;
    }

    public Mono<Double> convertCurrency(String fromCurrency, String toCurrency, double amount) {
        return webClient.post()
                .uri("/currencies/convert")
                .bodyValue(new CurrencyConversionRequest(fromCurrency, toCurrency, amount))
                .retrieve()
                .bodyToMono(CurrencyConversionResponse.class)
                .map(CurrencyConversionResponse::getConvertedAmount);
    }

    public Double convertCurrency2(String fromCurrency, String toCurrency, double amount) {
        String url = "http://localhost:8080/currencies/convert";

        CurrencyConversionRequest request = new CurrencyConversionRequest(fromCurrency, toCurrency, amount);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<CurrencyConversionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<CurrencyConversionResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                CurrencyConversionResponse.class
        );

        return response.getBody() != null ? response.getBody().getConvertedAmount() : null;
    }
}
