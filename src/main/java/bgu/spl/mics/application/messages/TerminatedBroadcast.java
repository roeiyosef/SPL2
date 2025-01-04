package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminatedBroadcast implements Broadcast {
    private String name;
    public TerminatedBroadcast(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
