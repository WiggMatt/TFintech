import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.matthew.lesson3.collection.CustomLinkedList;
import ru.matthew.lesson3.iterator.LinkedListIterator;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование итератора LinkedListIterator")
public class LinkedListIteratorTest {

    @Nested
    @DisplayName("Когда список пустой")
    class WhenListIsEmpty {

        private LinkedListIterator<Integer> iterator;

        @BeforeEach
        void setUp() {
            CustomLinkedList<Integer> emptyList = new CustomLinkedList<>();
            iterator = emptyList.createLinkedListIterator(emptyList.getHead());
        }

        @Test
        @DisplayName("hasNext() должен возвращать false")
        void testHasNextReturnsFalse() {
            assertFalse(iterator.hasNext(), "hasNext() должен возвращать false для пустого списка");
        }

        @Test
        @DisplayName("next() должен выбрасывать исключение")
        void testNextThrowsException() {
            assertThrows(NoSuchElementException.class, () -> iterator.next(),
                    "next() должен выбрасывать NoSuchElementException для пустого списка");
        }

        @Test
        @DisplayName("forEachRemaining не должен вызывать действие")
        void testForEachRemainingDoesNotExecute() {
            iterator.forEachRemaining(e -> fail("Должно быть никаких вызовов в пустом списке"));
        }
    }

    @Nested
    @DisplayName("Когда список содержит элементы")
    class WhenListHasElements {

        private LinkedListIterator<Integer> iterator;

        @BeforeEach
        void setUp() {
            CustomLinkedList<Integer> listWithElements = new CustomLinkedList<>();
            listWithElements.add(1);
            listWithElements.add(2);
            listWithElements.add(3);
            iterator = listWithElements.createLinkedListIterator(listWithElements.getHead());
        }

        @Test
        @DisplayName("Итерация должна возвращать все элементы")
        void testIterationReturnsAllElements() {
            assertTrue(iterator.hasNext(), "Итератор должен иметь следующий элемент");
            assertEquals(1, iterator.next(), "Первый элемент должен быть 1");

            assertTrue(iterator.hasNext(), "Итератор должен иметь следующий элемент");
            assertEquals(2, iterator.next(), "Второй элемент должен быть 2");

            assertTrue(iterator.hasNext(), "Итератор должен иметь следующий элемент");
            assertEquals(3, iterator.next(), "Третий элемент должен быть 3");

            assertFalse(iterator.hasNext(), "Итератор не должен иметь следующего элемента");
        }

        @Test
        @DisplayName("next() должен выбрасывать исключение после последнего элемента")
        void testNextThrowsExceptionAfterLastElement() {
            iterator.next(); // 1
            iterator.next(); // 2
            iterator.next(); // 3
            assertThrows(NoSuchElementException.class, () -> iterator.next(),
                    "next() должен выбрасывать NoSuchElementException после последнего элемента");
        }

        @Test
        @DisplayName("forEachRemaining должен выполнять действие для оставшихся элементов")
        void testForEachRemainingExecutesAction() {
            StringBuilder result = new StringBuilder();
            iterator.forEachRemaining(result::append);

            assertEquals("123", result.toString(), "forEachRemaining должен вызвать действие для всех оставшихся элементов");
        }
    }

    @Nested
    @DisplayName("Когда список содержит один элемент")
    class WhenListHasOneElement {

        private LinkedListIterator<Integer> iterator;

        @BeforeEach
        void setUp() {
            CustomLinkedList<Integer> singleElementList = new CustomLinkedList<>();
            singleElementList.add(1);
            iterator = singleElementList.createLinkedListIterator(singleElementList.getHead());
        }

        @Test
        @DisplayName("Итерация должна возвращать единственный элемент")
        void testIterationReturnsSingleElement() {
            assertTrue(iterator.hasNext(), "Итератор должен иметь следующий элемент");
            assertEquals(1, iterator.next(), "Единственный элемент должен быть 1");
            assertFalse(iterator.hasNext(), "Итератор не должен иметь следующего элемента");
        }

        @Test
        @DisplayName("next() должен выбрасывать исключение после единственного элемента")
        void testNextThrowsExceptionAfterSingleElement() {
            iterator.next(); // 1
            assertThrows(NoSuchElementException.class, () -> iterator.next(),
                    "next() должен выбрасывать NoSuchElementException после единственного элемента");
        }
    }
}
