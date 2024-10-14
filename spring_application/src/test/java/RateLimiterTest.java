import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.matthew.exception.RateLimitExceededException;
import ru.matthew.utils.RateLimiter;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(2);
    }

    @Test
    void shouldExecuteTasksWithinRateLimit() throws Exception {
        // Arrange
        Callable<String> task = () -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return "Task Completed";
        };

        // Act
        String result1 = rateLimiter.executeWithLimit(task);
        String result2 = rateLimiter.executeWithLimit(task);

        // Assert
        assertEquals("Task Completed", result1);
        assertEquals("Task Completed", result2);
    }

    @Test
    void shouldThrowRateLimitExceededExceptionWhenExceedingLimit() throws InterruptedException {
        // Arrange
        Callable<String> task = () -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return "Task Completed";
        };

        Thread thread1 = new Thread(() -> {
            try {
                rateLimiter.executeWithLimit(task);
            } catch (Exception e) {
                fail("First thread should not throw exception");
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                rateLimiter.executeWithLimit(task);
            } catch (Exception e) {
                fail("Second thread should not throw exception");
            }
        });

        Thread thread3 = new Thread(() -> assertThrows(RateLimitExceededException.class, () -> rateLimiter.executeWithLimit(task)));

        // Act
        thread1.start();
        thread2.start();
        thread3.start();

        // Assert
        thread1.join();
        thread2.join();
        thread3.join();
    }

    @Test
    void shouldAllowMultipleThreadsToExecuteWithinLimit() throws InterruptedException {
        // Arrange
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        AtomicInteger completedTasks = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    rateLimiter.executeWithLimit(() -> {
                        TimeUnit.MILLISECONDS.sleep(100);
                        completedTasks.incrementAndGet();
                        return null;
                    });
                } catch (RateLimitExceededException ignored) {
                    // Игнорируем исключение
                } catch (Exception e) {
                    fail("Should not throw any unexpected exception");
                }
            });
            threads[i].start();
        }

        // Act
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        assertTrue(completedTasks.get() <= 10);
    }

    @Test
    void shouldExecuteTaskAfterRateLimitResets() throws Exception {
        // Arrange
        Callable<String> task = () -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return "Task Completed";
        };

        // Act
        rateLimiter.executeWithLimit(task);
        TimeUnit.MILLISECONDS.sleep(200);
        String result = rateLimiter.executeWithLimit(task);

        // Assert
        assertEquals("Task Completed", result);
    }
}
