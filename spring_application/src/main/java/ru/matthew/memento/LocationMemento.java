package ru.matthew.memento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.matthew.model.Location;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LocationMemento implements Memento {
    private String slug;
    private String name;
    private String action;

    // Конструктор, создающий снимок из сущности Location
    public LocationMemento(Location location, String action) {
        this.slug = location.getSlug();
        this.name = location.getName();
        this.action = action;
    }

    @Override
    public String toString() {
        return "LocationMemento{action='" + action + ", name='" + name + "', slug='" + slug + "'}";
    }
}
