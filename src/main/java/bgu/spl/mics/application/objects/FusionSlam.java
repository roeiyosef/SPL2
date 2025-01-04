package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {



    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();
    }
    private LinkedList<LandMark> landmarks;
    private LinkedList<Pose> Poses;

    public FusionSlam(){
        this.landmarks = new LinkedList<>();
        this.Poses = new LinkedList<>();
    }
    public static  FusionSlam getInstance() {
        return FusionSlam.FusionSlamHolder.instance;
    }

    public LinkedList<LandMark> getLandmarks() {
        return landmarks;
    }

    public LinkedList<Pose> getPoses() {
        return Poses;
    }

    public void updatePose(Pose currentPose) {
        Poses.add(currentPose);
    }

    public void turnTrackedObjectsToLandmarks(LinkedList<TrackedObject> trackedObjects, Pose currentPose) {
        for (TrackedObject obj : trackedObjects) {
            System.out.println("Processing TrackedObject ID: " + obj.getId()
                    + ", Description: " + obj.getDescription());
            //StampedCloudPoints globalCoordinates = transformToGlobalCoordinates(obj, currentPose);
            LinkedList<CloudPoint> globalCoordinates = transformToGlobalCoordinates(obj, currentPose);
            System.out.println("global cordinates for : " + obj.getId() + " are : " + globalCoordinates);
            // Update or create landmark in the FusionSlam object
            if (doesLandmarkExist(obj.getId())) {
                System.out.println("the landmark : " + obj.getId() + " exists");
                refineLandmark(obj.getId(), globalCoordinates);
            } else {
                System.out.println("add landmark");
                addLandmark(obj.getId(), obj.getDescription(), globalCoordinates);
            }
        }
    }
    public LinkedList<CloudPoint> transformToGlobalCoordinates(TrackedObject obj, Pose pose) {
        double thetaRad = Math.toRadians(pose.getYaw());
        LinkedList<CloudPoint> cloudPoints = new LinkedList<>();
        for (CloudPoint point : obj.getCoordinates()) {
            double xGlobal = Math.cos(thetaRad) * point.getX() - Math.sin(thetaRad) * point.getY() + pose.getX();
            double yGlobal = Math.sin(thetaRad) * point.getX() + Math.cos(thetaRad) * point.getY() + pose.getY();
            cloudPoints.add(new CloudPoint(xGlobal,yGlobal));

        }
        return cloudPoints;
    }
    public void refineLandmark(String id, LinkedList<CloudPoint> newCoordinates) {
       LandMark landmarkToRefine = getLandmarkWithId(id);
        LinkedList<CloudPoint> existingCoordinates = landmarkToRefine.getCoordinates();
        int size = Math.min(existingCoordinates.size(), newCoordinates.size());

        LinkedList<CloudPoint> refinedCoordinates = new LinkedList<>();

        for (int i = 0; i < size; i++) {
            CloudPoint oldPoint = existingCoordinates.get(i);
            CloudPoint newPoint = newCoordinates.get(i);

            // Average coordinates
            double avgX = (oldPoint.getX() + newPoint.getX()) / 2;
            double avgY = (oldPoint.getY() + newPoint.getY()) / 2;

            refinedCoordinates.add(new CloudPoint(avgX, avgY));
        }
        // Update landmark with refined coordinates
        landmarkToRefine.setCoordinates(refinedCoordinates);
        System.out.println("Landmark " + id + " refined with new coordinates.");
        System.out.println("Landmark " + id + " old coordinates were : " + existingCoordinates.toString());
        System.out.println("Landmark " + id + " inputed coordinates were : " + newCoordinates.toString());

        System.out.println("Landmark " + id + " NEW!REFINED coordinates were : " + refinedCoordinates.toString());

    }

    private LandMark getLandmarkWithId(String id) {
        for (int i=0;i<this.landmarks.size() ;i++){
            if (this.landmarks.get(i).getId().equals(id)){
                return landmarks.get(i);
            }
        }
        return null;
    }


    public void addLandmark(String id, String description, LinkedList<CloudPoint> GlobalPoints) {
        this.landmarks.add(new LandMark(id,description,GlobalPoints));
        StatisticalFolder.getInstance().incrementLandmarks();
    }
    public boolean doesLandmarkExist(String id) {
        for (LandMark l : this.landmarks){
            if (l.getId().equals(id)){
                return true;
            }
        }
        return false;
    }
}
