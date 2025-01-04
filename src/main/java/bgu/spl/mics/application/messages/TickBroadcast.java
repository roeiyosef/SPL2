package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {

    private final int tickNumber;
    public TickBroadcast(int tickNumber) {
      this.tickNumber = tickNumber;
    }

    public int getTickNumber() {
        return tickNumber;
    }
}
