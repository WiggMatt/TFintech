package ru.matthew.service.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.matthew.aop.Timed;
import ru.matthew.model.Location;
import ru.matthew.model.PlaceCategory;
import ru.matthew.observer.listeners.Observer;
import ru.matthew.observer.publisher.Observable;

import java.util.List;

@Service
@Slf4j
public class DataInitializerService {

    private final ExternalDataLoaderService externalDataLoaderService;
    private final Observable<Location> locationObservable = new Observable<>();
    private final Observable<PlaceCategory> placeCategoryObservable = new Observable<>();

    @Autowired
    public DataInitializerService(ExternalDataLoaderService externalDataLoaderService,
                                  List<Observer<Location>> locationObservers,
                                  List<Observer<PlaceCategory>> placeCategoryObservers) {
        this.externalDataLoaderService = externalDataLoaderService;
        locationObservers.forEach(locationObservable::subscribe);
        placeCategoryObservers.forEach(placeCategoryObservable::subscribe);
    }

    @Timed
    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        log.info("Начало инициализации данных");

        try {
            initCategories();
            initLocations();
        } catch (Exception e) {
            log.error("Ошибка при инициализации данных", e);
        }

        log.info("Инициализация данных завершена");
    }

    private void initCategories() {
        List<PlaceCategory> placeCategories = externalDataLoaderService.fetchCategoriesFromApi();
        placeCategoryObservable.notify(placeCategories);
        log.info("Загружено категорий: {}", placeCategories.size());
    }

    private void initLocations() {
        List<Location> locations = externalDataLoaderService.fetchLocationsFromApi();
        locationObservable.notify(locations);
        log.info("Загружено городов: {}", locations.size());
    }
}
