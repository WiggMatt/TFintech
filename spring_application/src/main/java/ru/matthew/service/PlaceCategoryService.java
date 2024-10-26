package ru.matthew.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.matthew.exception.ElementAlreadyExistsException;
import ru.matthew.exception.ElementWasNotFoundException;
import ru.matthew.model.PlaceCategory;
import ru.matthew.memento.PlaceCategoryMemento;
import ru.matthew.repository.HistoryStore;
import ru.matthew.repository.InMemoryStore;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaceCategoryService {
    private final InMemoryStore<Integer, PlaceCategory> placeCategoryStore;
    private final HistoryStore<PlaceCategoryMemento> placeCategoriesHistory = new HistoryStore<>();


    public Collection<PlaceCategory> getAllPlaceCategories() {
        if (placeCategoryStore.getAll().isEmpty()) {
            log.warn("Список с категориями локаций пуст");
            throw new ElementWasNotFoundException("Список с категориями локаций пуст");
        }

        return placeCategoryStore.getAll();
    }

    public PlaceCategory getPlaceCategoryById(int id) {
        return placeCategoryStore.get(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка поиска категории локаций: категория с id {} не найдена", id);
                    return new ElementWasNotFoundException("Категория с таким id не найдена");
                });
    }

    public void createPlaceCategory(PlaceCategory placeCategory) {
        if (StringUtils.isBlank(placeCategory.getName()) || StringUtils.isBlank(placeCategory.getSlug()) ||
                placeCategory.getId() == 0) {
            log.error("Ошибка создания категории места: обязательные поля отсутствуют (id, name или slug)");
            throw new IllegalArgumentException("Поля id, name и slug обязательны");
        }
        if (placeCategoryStore.get(placeCategory.getId()).isPresent()) {
            log.warn("Ошибка создания категории места: категория с id {} уже существует", placeCategory.getId());
            throw new ElementAlreadyExistsException("Категория с таким id уже существует");
        }

        placeCategoryStore.save(placeCategory);
    }

    public void updatePlaceCategory(int id, PlaceCategory placeCategory) {
        if (placeCategory.getName() == null || placeCategory.getSlug() == null) {
            log.error("Ошибка обновления категории места: name или slug отсутствуют");
            throw new IllegalArgumentException("Поле name или slug обязательно");
        }

        PlaceCategory existingPlaceCategory = placeCategoryStore.get(id)
                .orElseGet(() -> {
                    log.warn("Ошибка обновления категории места: категория с id {} не найдена", id);
                    throw new ElementWasNotFoundException("Категория с таким id не найдена");
                });

        // Сохраняем снимок перед обновлением
        PlaceCategoryMemento memento = new PlaceCategoryMemento(existingPlaceCategory, "Обновление");
        placeCategoriesHistory.save(memento);

        placeCategory.setId(id);
        placeCategoryStore.update(placeCategory);
    }

    public void deletePlaceCategory(int id) {
        PlaceCategory placeCategory = placeCategoryStore.get(id)
                .orElseGet(() -> {
                    log.warn("Ошибка удаления категории места: категория с id {} не найдена", id);
                    throw new ElementWasNotFoundException("Категория места с таким id не найдена.");
                });

        // Сохраняем снимок перед удалением
        PlaceCategoryMemento memento = new PlaceCategoryMemento(placeCategory, "Удаление");
        placeCategoriesHistory.save(memento);

        placeCategoryStore.delete(id);
    }

    public Collection<PlaceCategoryMemento> getLocationHistory() {
        return placeCategoriesHistory.getHistory();
    }
}
