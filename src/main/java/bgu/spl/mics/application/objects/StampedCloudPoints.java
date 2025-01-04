package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private String id;
    private int time;
    private LinkedList<LinkedList<Double>> cloudPoints;
    public StampedCloudPoints(String id, int time, LinkedList<LinkedList<Double>> cloudPoints) {
        this.id = id;
        this.time = time;
        this.cloudPoints = cloudPoints;
    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public LinkedList<LinkedList<Double>> getCloudPoints() {
        return cloudPoints;
    }

    @Override
    public String toString() {
        return "StampedCloudPoints{" + "id='" + id + '\'' + ", time=" + time + ", cloudPoints=" + cloudPoints.toString() + "}";
    }
}
