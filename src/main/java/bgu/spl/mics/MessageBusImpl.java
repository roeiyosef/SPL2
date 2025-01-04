package bgu.spl.mics;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	//private static  MessageBusImpl instance = null;
	private static class SingletonHolder{
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	// Subscriptions
	private  final ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue <MicroService>> eventSubscribers = new ConcurrentHashMap<>();
	private  final ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue <MicroService>> broadcastSubscribers = new ConcurrentHashMap<>();

	// Message Queues
	private  final ConcurrentHashMap<MicroService, BlockingQueue<Message>> microserviceQueues = new ConcurrentHashMap<>();

	// Event-Future Mapping
	private  final ConcurrentHashMap<Event<?>, Future<?>> eventFutures = new ConcurrentHashMap<>();

	//a better approach to deal with futures
	//Excecutor Completion Service
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		//eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue <MicroService>());
		//eventSubscribers.get(type).add(m);
		eventSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		//broadcastSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue <>());
		//broadcastSubscribers.get(type).add(m);
		broadcastSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = (Future<T>) eventFutures.remove(e); // Atomic remove-and-get operation
		if (future != null) {
			future.resolve(result); // Safely resolve the Future
		} else {
			System.out.println("Warning: Tried to complete an event that was not tracked by eventFutures.");
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> subscribers = broadcastSubscribers.get(b.getClass());
		if (subscribers == null || subscribers.isEmpty()) {
			return;
		}

		for(MicroService subscriber :subscribers) {

			BlockingQueue<Message> subscriberQueue = microserviceQueues.get(subscriber);
			if (subscriberQueue != null) {
				if (!subscriberQueue.offer(b)) {
					System.out.println("Failed to add broadcast to " + subscriber);
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		ConcurrentLinkedQueue<MicroService> subscribers = eventSubscribers.get(e.getClass());
		if (subscribers==null || subscribers.isEmpty()){
			return null;
		}
		MicroService subscriber = subscribers.poll();
		if (subscriber == null) {
			return null; // No available subscriber (additional safety check)
		}
		subscribers.add(subscriber); // Add back to the end for round-robin

		// Get the subscriber's message queue
		BlockingQueue<Message> subscriberQueue = microserviceQueues.get(subscriber);
		if (subscriberQueue == null) {
			return null; // Should not happen, but better safe than sorry
		}

		// Create a Future object for the result
		Future<T> future = new Future<>();
		//eventFutures.put(e, future); // Store the Future for later resolution
		eventFutures.putIfAbsent(e, future);

		// Add the event to the subscriber's queue
		if (!subscriberQueue.offer(e)) {
			System.out.println("Failed to send event: Subscriber queue is full.");
			return null; // Gracefully handle the failure.
		}

		// Return the Future to the caller
		return future;
	}

	@Override
	public void register(MicroService m) {
		synchronized (this) {
			microserviceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (this) {
			microserviceQueues.remove(m);

			// Remove from event subscribers
			for (ConcurrentLinkedQueue<MicroService> queue : eventSubscribers.values()) {
				queue.remove(m);
			}

			// Remove from broadcast subscribers
			for (ConcurrentLinkedQueue<MicroService> queue : broadcastSubscribers.values()) {
				queue.remove(m);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> queue = microserviceQueues.get(m);
		if (queue == null) {
			throw new IllegalStateException("MicroService " + m.getName() + " is not registered with MessageBus.");
		}
		try{
			return queue.take();
		}
		catch (InterruptedException e) {
			// Restore the interrupted status
			Thread.currentThread().interrupt();
			System.out.println("MicroService " + m.getName() + " was interrupted while waiting for a message.");
			throw e; // Rethrow to signal the interruption
		}
	}

	//this is not good!!!!!1
	/*public static synchronized MessageBusImpl getInstance() {
		if (instance == null) {
			instance = new MessageBusImpl();
		}
		return instance;
	}*/
	public static  MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}
}
