import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import ru.matthew.SpringApplication;
import ru.matthew.dto.SuccessJsonDTO;
import ru.matthew.model.Location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SpringApplication.class)
public class LocationsControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    private static final WireMockContainer WIRE_MOCK_CONTAINER =
            new WireMockContainer(DockerImageName.parse("wiremock/wiremock:latest"))
            .withMappingFromResource("wiremock/place-categories.json")
            .withMappingFromResource("wiremock/locations.json");


    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        String wireMockUrl = String.format("https://%s:%d",
                WIRE_MOCK_CONTAINER.getHost(),
                WIRE_MOCK_CONTAINER.getMappedPort(8080));
        registry.add("categories-url", () -> wireMockUrl + "/public-api/v1.2/place-categories");
        registry.add("locations-url", () -> wireMockUrl + "/public-api/v1.4/locations");
    }

    @BeforeAll
    public static void setUp() {
        WIRE_MOCK_CONTAINER.start();
    }

    @AfterAll
    public static void tearDown() {
        WIRE_MOCK_CONTAINER.stop();
    }

    @Test
    void getAllLocationsShouldReturnListOfLocations() {
        // Arrange
        String url = "/api/v1/locations";

        // Act
        ResponseEntity<Location[]> response = restTemplate.getForEntity(url, Location[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    void getLocationBySlugShouldReturnLocation() {
        // Arrange
        String slug = "msk";
        String url = String.format("/api/v1/locations/%s", slug);

        // Act
        ResponseEntity<Location> response = restTemplate.getForEntity(url, Location.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSlug()).isEqualTo(slug); // Проверка соответствия slug
    }

    @Test
    void createLocationShouldReturnSuccessMessage() {
        // Arrange
        Location newLocation = new Location("new-slug", "New Location");
        String url = "/api/v1/locations";
        HttpEntity<Location> request = new HttpEntity<>(newLocation);

        // Act
        ResponseEntity<SuccessJsonDTO> response = restTemplate.postForEntity(url, request, SuccessJsonDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Локация успешно создана");
    }

    @Test
    void updateLocationShouldReturnSuccessMessage() {
        // Arrange
        String slug = "msk";
        Location initialLocation = new Location(slug, "Initial Location");
        String createUrl = "/api/v1/locations";
        HttpEntity<Location> createRequest = new HttpEntity<>(initialLocation);
        restTemplate.postForEntity(createUrl, createRequest, SuccessJsonDTO.class);

        Location updatedLocation = new Location(slug, "Updated Location");
        String url = String.format("/api/v1/locations/%s", slug);
        HttpEntity<Location> request = new HttpEntity<>(updatedLocation);

        // Act
        ResponseEntity<SuccessJsonDTO> response =
                restTemplate.exchange(url, HttpMethod.PUT, request, SuccessJsonDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Локация успешно обновлена");
    }

    @Test
    void deleteLocationShouldReturnSuccessMessage() {
        // Arrange
        String slug = "msk";
        String url = String.format("/api/v1/locations/%s", slug);

        // Act
        ResponseEntity<SuccessJsonDTO> response =
                restTemplate.exchange(url, HttpMethod.DELETE, null, SuccessJsonDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Локация успешно удалена");
    }
}
