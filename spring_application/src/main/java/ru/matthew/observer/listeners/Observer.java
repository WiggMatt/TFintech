package ru.matthew.observer.listeners;

import java.util.List;

public interface Observer<T> {
    void update(List<T> items);
}
