package bgu.spl.mics;

import org.junit.jupiter.api.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ **MessageBus Test Suite**
 * This suite tests core functionalities of the MessageBus:
 * 1. MicroService Registration and Event Handling
 * 2. Unregistration Behavior
 * 3. Round-Robin Event Distribution
 */
public class MessageBusTest {

    private MessageBusImpl messageBus;
    private MicroService mockService1;
    private MicroService mockService2;

    @BeforeEach
    public void setUp() {
        messageBus = MessageBusImpl.getInstance();
        mockService1 = new MockMicroService("Service1");
        mockService2 = new MockMicroService("Service2");
    }

    @AfterEach
    public void tearDown() {
        messageBus.unregister(mockService1);
        messageBus.unregister(mockService2);
    }

    /**
     * ✅ **Test 1: MicroService Registration and Event Handling**
     * Pre-condition: A MicroService is registered and subscribes to an Event type.
     * Post-condition: The MicroService successfully receives the event.
     */
    @Test
    public void testMicroServiceRegistrationAndEventHandling() throws InterruptedException {
        // Pre-condition
        messageBus.register(mockService1);
        messageBus.subscribeEvent(TestEvent.class, mockService1);

        TestEvent event = new TestEvent();
        messageBus.sendEvent(event);

        // Action
        Message receivedMessage = messageBus.awaitMessage(mockService1);

        // Post-condition
        assertNotNull(receivedMessage, "MicroService should receive a message");
        assertTrue(receivedMessage instanceof TestEvent, "Received message should be of type TestEvent");
    }

    /**
     * ✅ **Test 2: MicroService Unregistration**
     * Pre-condition: A MicroService is registered and then unregistered.
     * Post-condition: The MicroService no longer receives messages.
     */
    @Test
    public void testMicroServiceUnregistration() {
        // Pre-condition
        messageBus.register(mockService1);
        messageBus.subscribeEvent(TestEvent.class, mockService1);

        // Action
        messageBus.unregister(mockService1);

        // Post-condition
        assertThrows(IllegalStateException.class, () -> {
            messageBus.awaitMessage(mockService1);
        }, "MicroService should not receive messages after unregistration");
    }

    /**
     * ✅ **Test 3: Round-Robin Event Distribution**
     * Pre-condition: Multiple MicroServices are registered and subscribed to the same event.
     * Post-condition: Events are distributed in a round-robin fashion.
     */
    @Test
    public void testRoundRobinEventDistribution() throws InterruptedException {
        // Pre-condition
        messageBus.register(mockService1);
        messageBus.register(mockService2);
        messageBus.subscribeEvent(TestEvent.class, mockService1);
        messageBus.subscribeEvent(TestEvent.class, mockService2);

        TestEvent event1 = new TestEvent();
        TestEvent event2 = new TestEvent();

        messageBus.sendEvent(event1);
        messageBus.sendEvent(event2);

        // Action
        Message receivedByService1 = messageBus.awaitMessage(mockService1);
        Message receivedByService2 = messageBus.awaitMessage(mockService2);

        // Post-condition
        assertEquals(event1, receivedByService1, "Service1 should receive the first event");
        assertEquals(event2, receivedByService2, "Service2 should receive the second event");
    }

    // Mock Event class for testing
    private static class TestEvent implements Event<String> {}

    // Mock MicroService class for testing
    private static class MockMicroService extends MicroService {
        public MockMicroService(String name) {
            super(name);
        }

        @Override
        protected void initialize() {}
    }
}