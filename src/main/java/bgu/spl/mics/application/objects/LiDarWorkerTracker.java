package bgu.spl.mics.application.objects;


import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import java.util.LinkedList;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private int id;
    private int frequency;
    private STATUS status;

    private LinkedList<TrackedObject> lastTrackedObjects;

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new LinkedList<>();
       // StampedCloudPoints ls = LiDarDataBase.getInstance("").getCloudPointsByID();
    }
    public TrackedObjectsEvent processDetectObjectsEvent(DetectObjectsEvent event) {
        if (status != STATUS.UP) {
            System.out.println("LiDARWorker " + id + ": Cannot process event, worker is not operational.");
            return null;
        }

        LinkedList<TrackedObject> trackedObjectsList = new LinkedList<>();

        for (DetectedObject obj : event.getDetectedObjects().getDetectedObjects()) {
            StampedCloudPoints points = fetchCloudPoints(obj.getId(),event.getDetectedObjects().getTime());
                TrackedObject trackedObj = new TrackedObject(
                        obj.getId(),
                        event.getDetectedObjects().getTime(),
                        obj.getDescription(),
                        convert(points.getCloudPoints())
                );
                trackedObjectsList.add(trackedObj);
                LiDarDataBase.getInstance("").increaseCounter();
        }

        if (trackedObjectsList.isEmpty()) {
            System.out.println("LiDARWorker " + id + ": No valid objects were tracked from the event.");
            return null;
        }

        this.lastTrackedObjects = trackedObjectsList;
        return new TrackedObjectsEvent(trackedObjectsList);
    }
    private StampedCloudPoints fetchCloudPoints(String objectId,int tick) {
        return LiDarDataBase.getInstance("").getCloudPointsByIDAndTime(objectId,tick);
    }


    private LinkedList<CloudPoint> convert(LinkedList<LinkedList<Double>> scp){
        LinkedList<CloudPoint> cloudPoints = new LinkedList<>();
        for (LinkedList<Double> point : scp) {
                double x = point.get(0); // Take x as double
                double y = point.get(1); // Take y as double
                cloudPoints.add(new CloudPoint(x, y));
        }
        return cloudPoints;
    }
    /**
     * Marks the LiDAR as in error state.
     */
    public void markError() {
        this.status = STATUS.ERROR;
        System.out.println("LiDAR " + id + " encountered an error and is now marked as ERROR.");
    }

    /**
     * Marks the LiDAR as down (e.g., powered off).
     */
    public void markDown() {
        this.status = STATUS.DOWN;
        System.out.println("LiDAR " + id + " is now marked as DOWN.");
    }

    /**
     * Marks the LiDAR as operational again.
     */
    public void markUp() {
        this.status = STATUS.UP;
        System.out.println("LiDAR " + id + " is now operational (UP).");
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }


    @Override
    public String toString() {
        return "LiDarWorkerTracker{" +
                "id=" + id +
                ", frequency=" + frequency +
                ", status=" + status +
                ", lastTrackedObjects=" + lastTrackedObjects +
                '}';
    }

    public LinkedList<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }
}
