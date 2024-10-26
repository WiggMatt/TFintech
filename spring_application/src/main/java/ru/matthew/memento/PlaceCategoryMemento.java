package ru.matthew.memento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.matthew.model.PlaceCategory;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceCategoryMemento implements Memento {
    private int id;
    private String slug;
    private String name;
    private String action;

    // Конструктор, создающий снимок из сущности PlaceCategory
    public PlaceCategoryMemento(PlaceCategory placeCategory, String action) {
        this.id = placeCategory.getId();
        this.slug = placeCategory.getSlug();
        this.name = placeCategory.getName();
        this.action = action;
    }

    @Override
    public String toString() {
        return "PlaceCategoriesMemento{action='" + action + ", id= " + id + ", name='" + name + "', slug='" + slug + "'}";
    }
}
