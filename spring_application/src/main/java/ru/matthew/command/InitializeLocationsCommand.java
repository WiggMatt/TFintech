package ru.matthew.command;

import lombok.extern.slf4j.Slf4j;
import ru.matthew.model.Location;
import ru.matthew.observer.listeners.Observer;
import ru.matthew.observer.publisher.Observable;
import ru.matthew.service.external.ExternalDataLoaderService;

import java.util.List;

@Slf4j
public class InitializeLocationsCommand implements Command {
    private final ExternalDataLoaderService externalDataLoaderService;
    private final Observable<Location> locationObservable = new Observable<>();

    public InitializeLocationsCommand(ExternalDataLoaderService externalDataLoaderService,
                                      List<Observer<Location>> locationObservers) {
        this.externalDataLoaderService = externalDataLoaderService;
        locationObservers.forEach(locationObservable::subscribe);
    }

    @Override
    public void execute() {
        List<Location> locations = externalDataLoaderService.fetchLocationsFromApi();
        locationObservable.notify(locations);
        log.info("Загружено городов: {}", locations.size());
    }
}
