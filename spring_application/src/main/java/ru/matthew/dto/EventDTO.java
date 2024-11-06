package ru.matthew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.matthew.dao.model.Event;
import ru.matthew.dao.model.Location;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String title;

    @NotNull(message = "Дата не может быть пустая")
    private LocalDate date;

    private String description;

    @PositiveOrZero
    private BigDecimal price;

    @NotBlank(message = "Локация не может быть пустой")
    private String locationSlug;

    public static EventDTO fromEntity(Event event) {
        return new EventDTO(
                event.getId(),
                event.getTitle(),
                event.getDate(),
                event.getDescription(),
                event.getPrice(),
                event.getLocation().getSlug()
        );
    }

    public Event toEntity(Location location) {
        return new Event(
                this.id,
                this.title,
                this.date,
                this.description,
                this.price,
                location
        );
    }
}
