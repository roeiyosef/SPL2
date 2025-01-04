package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    private LiDarWorkerTracker lidarWorker;
    private int currentTick;
    public static AtomicInteger LidarCounter = new AtomicInteger(0);
    private final Map<Integer, DetectObjectsEvent> pendingEvents = new HashMap<>();

    private int numOfFinishedCameraServices;

    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LidarService " + LidarCounter.incrementAndGet());
        this.lidarWorker = LiDarWorkerTracker;
        this.currentTick = 0;
        this.numOfFinishedCameraServices = 0;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // 🕒 Handle TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tick -> {
            this.currentTick = tick.getTickNumber();
            System.out.println(this.getName() + ": Received TickBroadcast at tick " + currentTick);
            if (checkForSensorError()) {
                System.out.println(this.getName() + ": ERROR detected in LiDARDataBase at Tick " + currentTick);
                GurionRockRunner.isTerminatedEarly= new AtomicBoolean(true);

                //NOT REALLY DOING ANYTHING
                StatisticalFolder.getInstance().setSystemRuntime(this.currentTick);
                sendBroadcast(new CrashedBroadcast("lidar disconnected","lidar"+this.lidarWorker.getId(),this.currentTick));
                terminate(); // Stop LiDarService immediately
            }
            else {

                // 🚀 Continue with normal event processing
                processPendingEvents();

                if (checkIfSensorFinished()) {
                    System.out.println(this.getName() + ":finished his job at tick" + currentTick);
                    sendBroadcast(new TerminatedBroadcast(this.getName()));
                    terminate(); // Stop LiDarService immediately
                }
            }
        });

        // 🚨 Handle Termination
        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getName().equals("TimeService")) {
                System.out.println(this.getName() + ": Received TerminatedBroadcast. Shutting down...");
                terminate();
            }
            if ((terminated.getName()).startsWith("CameraService")) {
                this.numOfFinishedCameraServices++;
                if (checkIfSensorFinished()){
                    System.out.println(this.getName() + ":finished his job at tick"+ currentTick);
                    sendBroadcast(new TerminatedBroadcast(this.getName()));
                    terminate(); // Stop LiDarService immediately
                }
            }
        });

        // 🛡️ Handle Crashes
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            System.out.println(this.getName() + ": Received CrashedBroadcast. Shutting down...");
            crashed.getData().addLidarFrames("lidar" + this.lidarWorker.getId(),this.lidarWorker.getLastTrackedObjects());
            terminate();
        });

        // 📥 Handle DetectObjectsEvent
        subscribeEvent(DetectObjectsEvent.class, event -> {
            int scheduledTick = event.getDetectedObjects().getTime() + lidarWorker.getFrequency();
            pendingEvents.put(scheduledTick, event);
            System.out.println(this.getName() + ": Scheduled DetectObjectsEvent for Tick " + scheduledTick);
        });
    }

    private boolean checkIfSensorFinished() {
        if (this.numOfFinishedCameraServices==GurionRockRunner.NumOfCameras && this.pendingEvents.isEmpty()){
            System.out.println("SENSOR :" + this.getName() +" FINISHED ");
            System.out.println("THE NUM OF FINSIHED CAMERA SERVICES IS : " + this.numOfFinishedCameraServices);
            return true;
        }
        return false;
    }

    private boolean checkForSensorError() {
        LinkedList<StampedCloudPoints> cloudPointsAtTick =
                LiDarDataBase.getInstance("").getCloudPointsByTime(currentTick);

        if (!cloudPointsAtTick.isEmpty()) {
            for (StampedCloudPoints points : cloudPointsAtTick) {
                if ("ERROR".equals(points.getId())) {
                    return true; // Error found in LiDAR data
                }
            }
        }
        return false; // No errors detected
    }
    private void processPendingEvents() {
        Iterator<Map.Entry<Integer, DetectObjectsEvent>> iterator = pendingEvents.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, DetectObjectsEvent> entry = iterator.next();
            int scheduledTick = entry.getKey();

            if (scheduledTick <= currentTick) {
                DetectObjectsEvent event = entry.getValue();
                System.out.println("LiDARService: Processing DetectObjectsEvent scheduled for Tick " + scheduledTick);

                // Process the event via LiDARWorker
                TrackedObjectsEvent trackedEvent = lidarWorker.processDetectObjectsEvent(event);

                if (trackedEvent != null) {
                    System.out.println("LiDARService: Received TrackedObjectsEvent from LiDARWorker. Sending to Fusion-SLAM.");
                    for (int i=0;i<trackedEvent.getTrackedObjects().size();i++){
                        StatisticalFolder.getInstance().incrementTrackedObjects();
                    }
                    sendEvent(trackedEvent);
                } else {
                    System.out.println("LiDARService: Failed to process DetectObjectsEvent at Tick " + scheduledTick);
                }

                iterator.remove(); // Safe removal
            }
        }
    }
}
