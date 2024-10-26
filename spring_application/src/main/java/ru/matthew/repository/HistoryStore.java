package ru.matthew.repository;

import lombok.Getter;
import ru.matthew.memento.Memento;

import java.util.ArrayList;
import java.util.List;

@Getter
public class HistoryStore<T extends Memento> {
    private final List<T> history = new ArrayList<>();

    public void save(T memento) {
        history.add(memento);
    }
}
