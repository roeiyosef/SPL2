package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    private String id;
    private int time;

    private String description;
    private LinkedList<CloudPoint> coordinates;
  //  private LinkedList<LinkedList<Double>> coordinates;


    public TrackedObject(String id, int time, String description ,LinkedList<CloudPoint> coordinates) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }


    public LinkedList<CloudPoint> getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "TrackedObject{" +
                "id='" + id + '\'' +
                ", time=" + time +
          //      ", description='" + description + '\'' +
                ", coordinates=" + coordinates.toString() +
                '}';
    }
}