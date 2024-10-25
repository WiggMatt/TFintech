package ru.matthew.lesson3.iterator;

import ru.matthew.lesson3.collection.CustomLinkedList;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class LinkedListIterator<E> implements CustomIterator<E> {
    private CustomLinkedList.Node<E> current;

    public LinkedListIterator(CustomLinkedList.Node<E> head) {
        this.current = head;
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        E data = current.getData();
        current = current.getNext();
        return data;
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        while (hasNext()) {
            action.accept(next());
        }
    }
}
