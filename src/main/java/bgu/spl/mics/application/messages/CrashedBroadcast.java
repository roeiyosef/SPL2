package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.CrashData;
import bgu.spl.mics.application.objects.DetectedObject;

import java.util.LinkedList;

public class CrashedBroadcast implements Broadcast {
    private CrashData data;

    public CrashedBroadcast(String errorDescription, String faultySensor,int tick) {
        this.data = new CrashData(errorDescription,faultySensor,tick);
    }

    public CrashData getData() {
        return data;
    }
}
