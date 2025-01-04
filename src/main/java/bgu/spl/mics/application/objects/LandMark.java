package bgu.spl.mics.application.objects;

import java.util.Date;
import java.util.LinkedList;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private final String id;
    private final String description;
    private  LinkedList<CloudPoint> coordinates;

    public LandMark(String id,String description,LinkedList<CloudPoint> coordinates){
            this.id= id;
            this.description= description;
            this.coordinates=coordinates;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public LinkedList<CloudPoint> getCoordinates() {
        return coordinates;
    }
    public String toString() {
        return "Landmark{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", coordinates=" + coordinates.toString() +
                '}';
    }

    public void setCoordinates(LinkedList<CloudPoint> refinedCoordinates) {
        this.coordinates = refinedCoordinates;
    }
}
