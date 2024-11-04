package ru.matthew.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.matthew.dao.model.Event;
import ru.matthew.dto.EventDTO;
import ru.matthew.dto.SuccessJsonDTO;
import ru.matthew.service.EventService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@Slf4j
public class EventController {
    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Поиск событий по фильтрам
    @GetMapping
    public List<EventDTO> searchEvents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String locationSlug,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        log.debug("Запрос событий с фильтром");
        List<EventDTO> events = eventService.getEventsByFilter(title, locationSlug, fromDate, toDate);
        log.info("Успешно получены все события");
        return events;
    }

    // Создание нового события
    @PostMapping
    public SuccessJsonDTO createEvent(@Valid @RequestBody EventDTO eventDTO) {
        eventService.createEvent(eventDTO);
        log.info("Событие успешно создано");
        return new SuccessJsonDTO("Событие успешно создано");
    }

    // Обновление события
    @PutMapping("/{id}")
    public SuccessJsonDTO updateEvent(@PathVariable Long id, @RequestBody EventDTO eventDTO) {
        eventService.updateEvent(id, eventDTO);
        log.info("Событие с ID {} успешно обновлено", id);
        return new SuccessJsonDTO("Событие успешно обновлено");
    }

    // Удаление события
    @DeleteMapping("/{id}")
    public SuccessJsonDTO deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        log.info("Событие с ID {} успешно удалено", id);
        return new SuccessJsonDTO("Событие успешно удалено");
    }
}
