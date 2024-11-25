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
import ru.matthew.model.PlaceCategory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SpringApplication.class)
public class PlaceCategoriesControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    private static final WireMockContainer WIRE_MOCK_CONTAINER =
            new WireMockContainer(DockerImageName.parse("wiremock/wiremock:latest"))
            .withMappingFromResource("wiremock/place-categories.json");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        String wireMockUrl = String.format("https://%s:%d",
                WIRE_MOCK_CONTAINER.getHost(),
                WIRE_MOCK_CONTAINER.getMappedPort(8080));
        registry.add("categories-url", () -> wireMockUrl + "/public-api/v1.2/place-categories");
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
    void getAllPlaceCategoriesShouldReturnListOfPlaceCategories() {
        // Arrange
        String url = "/api/v1/places/categories";

        // Act
        ResponseEntity<PlaceCategory[]> response = restTemplate.getForEntity(url, PlaceCategory[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }

    @Test
    void getPlaceCategoryByIdShouldReturnPlaceCategory() {
        // Arrange
        int id = 89;
        String url = String.format("/api/v1/places/categories/%d", id);

        // Act
        ResponseEntity<PlaceCategory> response = restTemplate.getForEntity(url, PlaceCategory.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(id);
    }

    @Test
    void createPlaceCategoryShouldReturnSuccessMessage() {
        // Arrange
        PlaceCategory newCategory = new PlaceCategory(777, "new-slug", "NewCategory");
        String url = "/api/v1/places/categories";
        HttpEntity<PlaceCategory> request = new HttpEntity<>(newCategory);

        // Act
        ResponseEntity<SuccessJsonDTO> response = restTemplate.postForEntity(url, request, SuccessJsonDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Категория места успешно создана");
    }

    @Test
    void updatePlaceCategoryShouldReturnSuccessMessage() {
        // Arrange
        int id = 89;
        PlaceCategory updatedCategory = new PlaceCategory(id, "updated-slug", "Updated Category");
        String url = String.format("/api/v1/places/categories/%d", id);
        HttpEntity<PlaceCategory> request = new HttpEntity<>(updatedCategory);

        // Act
        ResponseEntity<SuccessJsonDTO> response =
                restTemplate.exchange(url, HttpMethod.PUT, request, SuccessJsonDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Категория места успешно обновлена");
    }

    @Test
    void deletePlaceCategoryShouldReturnSuccessMessage() {
        // Arrange
        int id = 123;
        String url = String.format("/api/v1/places/categories/%d", id);

        // Act
        ResponseEntity<SuccessJsonDTO> response =
                restTemplate.exchange(url, HttpMethod.DELETE, null, SuccessJsonDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Категория места успешно удалена");
    }
}
