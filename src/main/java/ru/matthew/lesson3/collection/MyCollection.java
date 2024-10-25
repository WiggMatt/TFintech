package ru.matthew.lesson3.collection;

import ru.matthew.lesson3.iterator.Iterable;

public interface MyCollection<E> extends Iterable<E> {
    int size();
    boolean isEmpty();
    void add(E e);
    void add(int index, E element);
    void remove(int index);
    E get(int index);
    boolean contains(E element);
}
