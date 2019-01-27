package com.armandasalmd.weeklyroutine.classes;

import com.squareup.otto.Bus;

public class BusStation {
    private static final int busCount = 4;
    private static Bus bus[] = new Bus[busCount];

    private static void prepareBusses() {
        for (int i = 0; i < busCount; i++)
            bus[i] = new Bus();
    }

    public static Bus getBus(int busId) {
        if (bus[0] == null)
            prepareBusses();
        if (busId < busCount)
            return bus[busId];
        else
            return new Bus();
    }

}
