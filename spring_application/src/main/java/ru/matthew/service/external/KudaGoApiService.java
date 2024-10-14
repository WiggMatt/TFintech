package ru.matthew.service.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.matthew.dto.EventDTO;
import ru.matthew.dto.EventResponseDTO;
import ru.matthew.exception.ServiceUnavailableException;
import ru.matthew.utils.RateLimiter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class KudaGoApiService {
    @Value("${kudago.api.url}")
    private String kudaGoApiUrl;

    private final WebClient webClient;
    private final RateLimiter rateLimiter;

    private final RestTemplate restTemplate;
    private final ExecutorService executorService;

    @Autowired
    public KudaGoApiService(WebClient.Builder webClientBuilder, RateLimiter rateLimiter,
                            RestTemplate restTemplate,
                            @Qualifier("dataInitFixedThreadPool") ExecutorService executorService) {
        this.webClient = webClientBuilder.build();
        this.rateLimiter = rateLimiter;
        this.restTemplate = restTemplate;
        this.executorService = executorService;
    }

    public Flux<EventDTO> fetchEvents(LocalDate fromDate, LocalDate toDate) {
        String requestUrl = buildRequestUrl(fromDate, toDate);

        return Mono.fromCallable(() -> rateLimiter.executeWithLimit(() -> webClient.get()
                        .uri(requestUrl)
                        .retrieve()
                        .bodyToMono(EventResponseDTO.class)
                        .block()))
                .flatMapMany(response -> {
                    if (response != null && response.getResults() != null) {
                        return Flux.fromIterable(response.getResults());
                    } else {
                        return Flux.error(new ServiceUnavailableException("Ответ от KudaGo пустой", 3600));
                    }
                })
                .doOnError(e -> log.error("Ошибка при запросе событий: {}", e.getMessage(), e))
                .onErrorMap(e -> new ServiceUnavailableException("Ошибка при запросе событий в KudaGo API", 3600));
    }

    public CompletableFuture<List<EventDTO>> fetchEventsAsync(LocalDate fromDate, LocalDate toDate) {
        String requestUrl = buildRequestUrl(fromDate, toDate);

        return CompletableFuture.supplyAsync(() -> {
            log.debug("Запрос событий по URL: {}", requestUrl);
            EventResponseDTO response = restTemplate.getForObject(requestUrl, EventResponseDTO.class);
            return response != null ? response.getResults() : List.of();
        }, executorService);
    }


    private String buildRequestUrl(LocalDate fromDate, LocalDate toDate) {
        return String.format("%s?fields=id,title,description,place,dates,price&location=kzn&actual_since=%s&actual_until=%s",
                kudaGoApiUrl,
                fromDate.atStartOfDay(ZoneId.of("UTC")).toEpochSecond(),
                toDate.atStartOfDay(ZoneId.of("UTC")).toEpochSecond());
    }
}
