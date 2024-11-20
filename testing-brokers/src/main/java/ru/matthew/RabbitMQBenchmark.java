package ru.matthew;

import com.rabbitmq.client.*;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class RabbitMQBenchmark {

    private static final String QUEUE_NAME = "test_queue";
    private static final int MESSAGE_COUNT = 1000;

    private Connection connection;
    private Channel channel;
    private byte[] smallMessage;
    private byte[] largeMessage;

    private static final AtomicInteger messageCount = new AtomicInteger(0);

    @Setup
    public void setup() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("user");
        factory.setPassword("password");
        connection = factory.newConnection();
        channel = connection.createChannel();
        setupQueue(channel);

        smallMessage = new byte[100];
        largeMessage = new byte[10_000];
    }

    public static void setupQueue(Channel channel) throws IOException {
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    }

    public static void produceMessages(Channel channel, int count, byte[] message) throws IOException {
        for (int i = 0; i < count; i++) {
            channel.basicPublish("", QUEUE_NAME, null, message);
        }
    }

    public static void consumeMessages(Channel channel) throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            long deliveryTag = delivery.getEnvelope().getDeliveryTag();
            confirmMessage(channel, deliveryTag);
            messageCount.incrementAndGet();
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }

    @Benchmark
    public void testSingleProducerSingleConsumerSmallMessage() throws Exception {
        produceMessages(channel, MESSAGE_COUNT, smallMessage);
        consumeMessages(channel);
    }

    @Benchmark
    public void testSingleProducerSingleConsumerLargeMessage() throws Exception {
        produceMessages(channel, MESSAGE_COUNT, largeMessage);
        consumeMessages(channel);
    }

    // 3 продюсера и 1 консюмер (Load balancing)
    @Benchmark
    public void testMultipleProducersSingleConsumerSmallMessage() throws Exception {
        for (int i = 0; i < 3; i++) {
            produceMessages(channel, MESSAGE_COUNT / 3, smallMessage);
        }
        consumeMessages(channel);
    }

    @Benchmark
    public void testMultipleProducersSingleConsumerLargeMessage() throws Exception {
        for (int i = 0; i < 3; i++) {
            produceMessages(channel, MESSAGE_COUNT / 3, largeMessage);
        }
        consumeMessages(channel);
    }

    // 1 продюсер и 3 консюмера (Multiple consumers)
    @Benchmark
    public void testSingleProducerMultipleConsumersSmallMessage() throws Exception {
        produceMessages(channel, MESSAGE_COUNT, smallMessage);
        for (int i = 0; i < 3; i++) {
            consumeMessages(channel);
        }
    }

    @Benchmark
    public void testSingleProducerMultipleConsumersLargeMessage() throws Exception {
        produceMessages(channel, MESSAGE_COUNT, largeMessage);
        for (int i = 0; i < 3; i++) {
            consumeMessages(channel);
        }
    }

    // 3 продюсера и 3 консюмера (Load balancing + multiple consumers)
    @Benchmark
    public void testMultipleProducerMultipleConsumersSmallMessage() throws Exception {
        for (int i = 0; i < 3; i++) {
            produceMessages(channel, MESSAGE_COUNT / 3, smallMessage);
        }

        for (int i = 0; i < 3; i++) {
            consumeMessages(channel);
        }
    }

    @Benchmark
    public void testMultipleProducerMultipleConsumersLargeMessage() throws Exception {
        for (int i = 0; i < 3; i++) {
            produceMessages(channel, MESSAGE_COUNT / 3, largeMessage);
        }

        for (int i = 0; i < 3; i++) {
            consumeMessages(channel);
        }
    }

    // 10 продюсеров и 10 консюмеров (Stress test)
    @Benchmark
    public void testStressTestWithSmallMessage() throws Exception {
        for (int i = 0; i < 10; i++) {
            produceMessages(channel, MESSAGE_COUNT / 10, smallMessage);
        }
        for (int i = 0; i < 10; i++) {
            consumeMessages(channel);
        }
    }

    @Benchmark
    public void testStressTestWithLargeMessage() throws Exception {
        for (int i = 0; i < 10; i++) {
            produceMessages(channel, MESSAGE_COUNT / 10, largeMessage);
        }
        for (int i = 0; i < 10; i++) {
            consumeMessages(channel);
        }
    }

    public static void confirmMessage(Channel channel, long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    @TearDown
    public void tearDown() throws Exception {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }
}
