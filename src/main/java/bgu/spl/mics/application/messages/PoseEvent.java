package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

public class PoseEvent implements Event<Boolean> {

    private Pose currentPose;
    public PoseEvent(Pose currentPose) {
        this.currentPose = currentPose;
    }

    public Pose getPose() {
        return this.currentPose;
    }
}
