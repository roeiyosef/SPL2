package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.LinkedList;

public class DetectObjectsEvent implements Event<Boolean> {
   /* private LinkedList<DetectedObject> detectedObjects;
    private int time;*/

    private StampedDetectedObjects detectedObjects;

    public DetectObjectsEvent(StampedDetectedObjects detectedObjects) {
        this.detectedObjects = detectedObjects;
    }

    public StampedDetectedObjects getDetectedObjects() {
        return detectedObjects;
    }
}
