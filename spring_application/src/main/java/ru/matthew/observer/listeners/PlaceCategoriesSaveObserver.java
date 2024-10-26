package ru.matthew.observer.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.matthew.model.PlaceCategory;
import ru.matthew.service.PlaceCategoryService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceCategoriesSaveObserver implements Observer<PlaceCategory> {
    private final PlaceCategoryService placeCategoryService;

    @Override
    public void update(List<PlaceCategory> placeCategories) {
        log.info("PlaceCategoriesSaveObserver: Сохранение {} категорий в хранилище", placeCategories.size());
        placeCategories.forEach(placeCategoryService::createPlaceCategory);
    }
}
