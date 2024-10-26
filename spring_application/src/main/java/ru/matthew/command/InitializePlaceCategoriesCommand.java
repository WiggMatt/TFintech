package ru.matthew.command;

import lombok.extern.slf4j.Slf4j;
import ru.matthew.model.PlaceCategory;
import ru.matthew.observer.listeners.Observer;
import ru.matthew.observer.publisher.Observable;
import ru.matthew.service.external.ExternalDataLoaderService;

import java.util.List;

@Slf4j
public class InitializePlaceCategoriesCommand implements Command {
    private final ExternalDataLoaderService externalDataLoaderService;
    private final Observable<PlaceCategory> placeCategoryObservable = new Observable<>();

    public InitializePlaceCategoriesCommand(ExternalDataLoaderService externalDataLoaderService,
                                            List<Observer<PlaceCategory>> placeCategoryObservers) {
        this.externalDataLoaderService = externalDataLoaderService;
        placeCategoryObservers.forEach(placeCategoryObservable::subscribe);
    }

    @Override
    public void execute() {
        List<PlaceCategory> placeCategories = externalDataLoaderService.fetchCategoriesFromApi();
        placeCategoryObservable.notify(placeCategories);
        log.info("Загружено категорий: {}", placeCategories.size());
    }
}
