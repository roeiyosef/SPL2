package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private static class SingletonHolder{
        private static StatisticalFolder instance = new StatisticalFolder();
    }

    private AtomicInteger systemRuntime;
    private AtomicInteger numDetectedObjects;
    private AtomicInteger numTrackedObjects;
    private AtomicInteger numLandmarks;

    // Constructor
    public StatisticalFolder() {
        systemRuntime = new AtomicInteger(0);
          numDetectedObjects= new AtomicInteger(0);
         numTrackedObjects= new AtomicInteger(0);
         numLandmarks= new AtomicInteger(0);
    }

    // Increment methods
    public void incrementDetectedObjects() {
        numDetectedObjects.incrementAndGet();
    }

    public void incrementTrackedObjects() {
        numTrackedObjects.incrementAndGet();
    }

    public void incrementLandmarks() { // Fixed Typo
        numLandmarks.incrementAndGet();
    }

    public void incrementSystemRuntime() {
        systemRuntime.incrementAndGet();
    }

    // Getter methods
    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getSystemRuntime() {
        return systemRuntime.get();
    }



    public void setSystemRuntime(int systemRuntime) {
        this.systemRuntime = new AtomicInteger(systemRuntime);
    }


    @Override
    public String toString() {
        return "StatisticalFolder{" +
                "systemRuntime=" + systemRuntime.get() +
                ", numDetectedObjects=" + numDetectedObjects.get() +
                ", numTrackedObjects=" + numTrackedObjects.get() +
                ", numLandmarks=" + numLandmarks.get() +
                '}';
    }
    public static StatisticalFolder getInstance() {
        return StatisticalFolder.SingletonHolder.instance;
    }

}
