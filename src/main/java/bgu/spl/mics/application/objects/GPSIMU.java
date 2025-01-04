package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private STATUS status;
    private LinkedList<Pose> poseList;


    public GPSIMU(int currentTick) {
        this.status = STATUS.UP;
        this.poseList = new LinkedList<>();
    }


    public void addPose(Pose pose) {
        poseList.add(pose);
    }

   /* public Pose getLatestPose() {
        if (poseList.isEmpty()) {
            return null;
        }
        return poseList.get(poseList.size() - 1);
    }*/

    public STATUS getStatus() {
        return status;
    }


    public void setStatus(STATUS status) {
        this.status = status;
    }


    public LinkedList<Pose> getPoseList() {
        return poseList;
    }

    public LinkedList<Pose> getPoseListTillTick(int tick) {
      LinkedList<Pose>  list = new LinkedList<>();
        for (Pose pose : this.poseList) {
            if (pose.getTime()<=tick){
                list.add(pose);
            }
        }
        return list;
    }
    public void setPoseList(LinkedList<Pose> poses){
        this.poseList = poses;

    }
    @Override
    public String toString() {
        return "GPSIMU{" +
                ", status=" + status +
                ", poseList=" + poseList.toString() +
                '}';
    }
}
