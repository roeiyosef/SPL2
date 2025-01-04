package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.LinkedList;

public class TrackedObjectsEvent implements Event<Boolean> {
    private final LinkedList<TrackedObject> trackedObjects;

    public TrackedObjectsEvent(LinkedList<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }
    public LinkedList<TrackedObject> getTrackedObjects() {
        return this.trackedObjects;
    }


}
