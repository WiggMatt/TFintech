package ru.matthew.lesson3.iterator;

import ru.matthew.lesson3.collection.CustomLinkedList;

public interface Iterable<E> {
    LinkedListIterator<E> createLinkedListIterator(CustomLinkedList.Node<E> head);
}
