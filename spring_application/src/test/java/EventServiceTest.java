import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.matthew.dto.EventDTO;
import ru.matthew.dto.EventResponseDTO;
import ru.matthew.service.CurrencyService;
import ru.matthew.service.DateService;
import ru.matthew.service.EventService;
import ru.matthew.service.external.KudaGoApiService;
import ru.matthew.utils.RateLimiter;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventServiceTest {

    private AutoCloseable mocks;

    @InjectMocks
    private EventService eventService;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private DateService dateService;

    @Mock
    private KudaGoApiService kudaGoApiService;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testFetchEvents_withValidData_returnsFilteredEvents() throws Exception {
        // Arrange
        double budget = 1000;
        String currency = "USD";
        double convertedBudget = 75000;
        List<EventDTO> mockEvents = List.of(
                new EventDTO(1, "Event 1", "Description 1", null, "RUB", "50000"),
                new EventDTO(2, "Event 2", "Description 2", null, "RUB", "80000")
        );

        // Мокаем вызовы для получения данных
        when(rateLimiter.executeWithLimit(any())).thenReturn(mockEvents);
        when(currencyService.convertBudget(anyDouble(), anyString())).thenReturn(Mono.just(convertedBudget));
        when(dateService.determineDates(any(), any())).thenReturn(new LocalDate[]{LocalDate.now(), LocalDate.now().plusDays(7)});
        when(kudaGoApiService.fetchEvents(any(), any())).thenReturn(Flux.fromIterable(mockEvents));

        // Act
        Mono<EventResponseDTO> result = eventService.fetchEvents(budget, currency, "2024-01-01", "2024-01-07");

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getCount() == 1 && response.getResults().size() == 1)
                .verifyComplete();
    }

    @Test
    void testFetchEvents_withNoEvents_returnsEmptyList() throws Exception {
        // Arrange
        double budget = 1000;
        String currency = "USD";
        double convertedBudget = 75000;

        when(rateLimiter.executeWithLimit(any())).thenReturn(List.of());
        when(currencyService.convertBudget(anyDouble(), anyString())).thenReturn(Mono.just(convertedBudget));
        when(dateService.determineDates(any(), any())).thenReturn(new LocalDate[]{LocalDate.now(), LocalDate.now().plusDays(7)});
        when(kudaGoApiService.fetchEvents(any(), any())).thenReturn(Flux.empty());

        // Act
        Mono<EventResponseDTO> result = eventService.fetchEvents(budget, currency, "2024-01-01", "2024-01-07");

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getCount() == 0 && response.getResults().isEmpty())
                .verifyComplete();
    }
}
