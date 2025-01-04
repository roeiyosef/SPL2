package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private int id;
    private int frequency;
    private STATUS status;
    private LinkedList<StampedDetectedObjects> detectedObjectsList;
    private String errorDescription;

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.detectedObjectsList = new LinkedList<>();
        errorDescription="";
    }
    public StampedDetectedObjects prepareDetectedObjects(int currentTick) {
        LinkedList<DetectedObject> detectedObjects = getDetectedObjectsAtTick(currentTick);
        if (detectedObjects == null) {
            return null; // No objects to send
        }
        if (detectedObjects.isEmpty()){
            return getStampedDetectedObjectsAtTick(currentTick);
        }

        int errorIndex = ErrorInDetectedObjects(detectedObjects);
        if (errorIndex != -1) {
            markError(detectedObjects.get(errorIndex).getDescription()); // Handle the error
            return null; // Stop further processing
        }

        return getStampedDetectedObjectsAtTick(currentTick);
    }

 /*   public LinkedList<DetectedObject> getDetectedObjectAtNextTick(int tick) {
        if (tick>=0) {
            for (StampedDetectedObjects sdo : detectedObjectsList) {
                if (sdo.getTime() == tick) {
                    return sdo.getDetectedObjects();
                }
            }
        }
        return new LinkedList<>();
    }*/
    public StampedDetectedObjects getStampedDetectedObjectsAtTick(int tick) {
        if (tick>=0) {
            for (StampedDetectedObjects sdo : detectedObjectsList) {
                if (sdo.getTime() == tick) {
                    return sdo;
                }
            }
        }
        return null;
    }

    public LinkedList<DetectedObject> getDetectedObjectsAtTick(int tick) {
        if (tick>=0) {
            for (StampedDetectedObjects sdo : detectedObjectsList) {
                if (sdo.getTime() == tick) {
                    return sdo.getDetectedObjects();
                }
            }
        }
        return null;
    }
    /*public void addDetectedObjects(StampedDetectedObjects detectedObjects) {
         this.detectedObjectsList.add(detectedObjects);
    }*/

    // Getters
    public int getId() { return id; }
    public int getFrequency() { return frequency; }
    public STATUS getStatus() { return status; }
    public LinkedList<StampedDetectedObjects> getDetectedObjectsList() { return detectedObjectsList; }

    // Setters
    public void setStatus(STATUS status) { this.status = status; }

    public void setListOfStampedDetectedObjects(LinkedList<StampedDetectedObjects> stampedDetectedObjectsList) {
        this.detectedObjectsList = stampedDetectedObjectsList;
    }

    public void markDown() {
        this.status = STATUS.DOWN;
        System.out.println("Camera " + id + " is done with its missons and is now done.");
    }
    public void markError(String description) {
        this.status = STATUS.ERROR;
        this.errorDescription = description;
        System.out.println("Camera " + id + " encountered an error and is now marked as ERROR.");
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public int ErrorInDetectedObjects(LinkedList<DetectedObject> list) {
       // System.out.println("CHECKING ERROR IN " + this.getName() + "  +  LIST : " + list.toString());
        for (int i=0;i<list.size();i++){
            if (list.get(i).getId().equals("ERROR")){
                System.out.println("Found it it is at : " + list.get(i).toString());
                return i;
            }
        }
        //if the list is Valid!
        return -1;
    }

  /*  public int getLatestDetecetionTime() {
        int maxTime = this.detectedObjectsList.get(0).getTime();
        for (int i=1;i<this.detectedObjectsList.size();i++){
            if (this.detectedObjectsList.get(i).getTime()>maxTime){
                maxTime = this.detectedObjectsList.get(i).getTime();
            }
        }
        maxTime = maxTime + frequency;
        return maxTime;
    }*/
}
