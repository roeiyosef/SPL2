package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    private int tickSpeed;
    private int duration;
    private final Object tickLock = new Object();
    private AtomicInteger acknowledgedServices = new AtomicInteger(0);

    private int currentTick;

    public TimeService(int TickTime, int Duration) {
        super("TimeService");
       this.tickSpeed = TickTime;
       this.duration = Duration;
       this.currentTick = 0;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        //    Thread t1 =  new Thread(() -> {
          try {
       // int numOfTicks = 0;
        while (currentTick < duration && !GurionRockRunner.isTerminatedEarly.get()) {
            sendBroadcast(new TickBroadcast(currentTick));
            currentTick++;
            System.out.println("Sent TickBrodcast with currentTick value = " + currentTick);
            StatisticalFolder.getInstance().incrementSystemRuntime();
            Thread.sleep(tickSpeed);
        }

        if (GurionRockRunner.isTerminatedEarly.get()){
            // fusionSlam finsihed early
            System.out.println("FusionSlam terminated early - finsihed");
        }
        else{
            // After finishing all ticks, send TerminatedBroadcast
            System.out.println("TimeService completed all ticks and broadcasted termination.");
        }


    } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("TimeService was interrupted and is now terminating.");
            } finally {
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();
            }

    }

}
