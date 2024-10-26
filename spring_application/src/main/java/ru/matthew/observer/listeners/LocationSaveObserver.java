package ru.matthew.observer.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.matthew.model.Location;
import ru.matthew.service.LocationService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationSaveObserver implements Observer<Location> {

    private final LocationService locationService;

    @Override
    public void update(List<Location> locations) {
        log.info("LocationSaveObserver: Сохранение {} городов в хранилище", locations.size());
        locations.forEach(locationService::createLocation);
    }
}
