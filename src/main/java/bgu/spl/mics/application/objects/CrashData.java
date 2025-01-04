package bgu.spl.mics.application.objects;

import java.util.HashMap;
import java.util.LinkedList;

public class CrashData {

    private final int tickOfError;
    private final String errorMessage;
    private final String faultySensor;
    private final HashMap<String, StampedDetectedObjects> camerasLastFrames;
    private final HashMap<String, LinkedList<TrackedObject>> lidarsRecentCloudPoints;
    private final LinkedList<Pose> posesTillTick;

    // Constructor
    public CrashData(String errorMessage, String faultySensor, int tickOfError) {
        this.errorMessage = errorMessage;
        this.faultySensor = faultySensor;
        this.tickOfError = tickOfError;
        this.camerasLastFrames = new HashMap<>();
        this.lidarsRecentCloudPoints = new HashMap<>();
        this.posesTillTick = new LinkedList<>();
    }

    // Getters
    public int getTickOfError() {
        return tickOfError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getFaultySensor() {
        return faultySensor;
    }

    public HashMap<String, StampedDetectedObjects> getCamerasLastFrames() {
        return camerasLastFrames;
    }

    public HashMap<String, LinkedList<TrackedObject>> getLidarsRecentCloudPoints() {
        return lidarsRecentCloudPoints;
    }

    public LinkedList<Pose> getPosesTillTick() {
        return posesTillTick;
    }

    // Data Aggregation Methods
    public synchronized void addCameraFrames(String cameraId, StampedDetectedObjects frames) {
        if (frames != null) {
            this.camerasLastFrames.put(cameraId, frames);
        }
    }

    public synchronized void addLidarFrames(String lidarId, LinkedList<TrackedObject> trackedObjects) {
        if (trackedObjects != null) {
            this.lidarsRecentCloudPoints.put(lidarId, trackedObjects);
        }
    }

    public synchronized void addPoses(LinkedList<Pose> poses) {
        if (poses != null && !poses.isEmpty()) {
            this.posesTillTick.addAll(poses);
        }
    }

    // Completion Check
    public synchronized boolean isComplete(int totalCameras, int totalLidars) {
        return camerasLastFrames.size() == totalCameras &&
                lidarsRecentCloudPoints.size() == totalLidars &&
                !posesTillTick.isEmpty();
    }

    // For Debugging
    @Override
    public String toString() {
        return "CrashData{" +
                "tickOfError=" + tickOfError +
                ", errorMessage='" + errorMessage + '\'' +
                ", faultySensor='" + faultySensor + '\'' +
                ", camerasLastFrames=" + camerasLastFrames.size() +
                ", lidarsRecentCloudPoints=" + lidarsRecentCloudPoints.size() +
                ", posesTillTick=" + posesTillTick.size() +
                '}';
    }
}
