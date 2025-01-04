package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;

import java.util.LinkedList;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */

    private int currentTick;
    private GPSIMU gpsimu;

    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.currentTick = 0;
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tick -> {
            currentTick = tick.getTickNumber();
            System.out.println("PoseService - Recived TickBrodcast at tick : "  + currentTick);
            Pose currentPose = getPoseAtCurrentTick();
            if (currentPose != null) {
                sendEvent(new PoseEvent(currentPose));
                System.out.println(getName() + " sent PoseEvent at tick " + currentTick);
            }
            if (currentPose == null) {
                System.out.println(getName() + " could not find a pose for tick " + currentTick);
            }
        });

        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getName().equals("FusionSlamService") ||terminated.getName().equals("TimeService") ){
                terminate();
            }
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crash -> {
            LinkedList<Pose> posesTillTick = this.gpsimu.getPoseListTillTick(crash.getData().getTickOfError());
            crash.getData().addPoses(posesTillTick);
            terminate();
        });
    }

    private Pose getPoseAtCurrentTick() {
        for (Pose p : gpsimu.getPoseList()) {
            if (p.getTime() == currentTick) {
                return p;
            }
        }
        return null;
    }

    public GPSIMU getGpsimu() {
        return gpsimu;
    }

    public int getCurrentTick() {
        return currentTick;
    }
}
