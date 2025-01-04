package bgu.spl.mics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 *
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	private AtomicReference<T>  result;
	private AtomicBoolean isDone;

	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		result = new AtomicReference<>();
		isDone = new AtomicBoolean(false);
	}

	/**
	 * retrieves the result the Future object holds if it has been resolved.
	 * This is a blocking method! It waits for the computation in case it has
	 * not been completed.
	 * <p>
	 * @return return the result of type T if it is available, if not wait until it is available.
	 *
	 */
	public T get() {
		///synchronized (this) {
		while (!isDone.get()) {
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.out.println("Thread interrupted while waiting for result.");
			}
		}
		return result.get();
		//}
	}

	/**
	 * Resolves the result of this Future object.
	 */
	public void resolve (T result) {
		T oldVal;
		T newVal;
		do {
			oldVal = this.result.get();
			newVal = result;
		} while (!this.result.compareAndSet(oldVal,newVal));
		isDone = new AtomicBoolean(true);
	}
	/*public void resolve(T result) {
		synchronized (this) {
			if (!isDone.get()) {
				this.result.set(result); // Atomically set the result
				isDone.set(true);        // Mark the Future as resolved
				notifyAll();             // Wake up all waiting threads
			}
		}
	}*/
	/**
	 * @return true if this object has been resolved, false otherwise
	 */
	public boolean isDone() {
		return isDone.get();
	}

	/**
	 * retrieves the result the Future object holds if it has been resolved,
	 * This method is non-blocking, it has a limited amount of time determined
	 * by {@code timeout}
	 * <p>
	 * @param timout 	the maximal amount of time units to wait for the result.
	 * @param unit		the {@link TimeUnit} time units to wait.
	 * @return return the result of type T if it is available, if not,
	 * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
	 *         elapsed, return null.
	 */

	public T get(long timeout, TimeUnit unit) {
		long timeoutMillis = unit.toMillis(timeout);
		long endTime = System.currentTimeMillis() + timeoutMillis;

		//synchronized (this) {
		while (!isDone.get() && System.currentTimeMillis() < endTime) {
			try {
				long remainingTime = endTime - System.currentTimeMillis();
				if (remainingTime > 0) {
					wait(remainingTime);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.out.println("Thread interrupted while waiting for result with timeout.");
				return null;
			}
		}
		return result.get();
		//}
	}

}
