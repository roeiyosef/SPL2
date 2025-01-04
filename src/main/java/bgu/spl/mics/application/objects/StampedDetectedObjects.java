package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {

 private int time;
 private LinkedList<DetectedObject> DetectedObjects;

 public StampedDetectedObjects(int time, LinkedList<DetectedObject> DetectedObjects){
   this.time = time;
   this.DetectedObjects = DetectedObjects;
 }
 public LinkedList<DetectedObject> getDetectedObjects() {
  return DetectedObjects;
 }

 public int getTime() {
  return time;
 }
 public String toString() {
  return "StampedDetectedObjects{" +
          "time=" + time +
          ", detectedObjects=" + DetectedObjects.toString() +
          '}';
 }
}
