package de.tum.nst.model;

/**
 * Created by Francois on 02/11/2016.
 */

public class DVSevent {
    public int x = 0;
    public int y = 0;
    boolean polarity = false;
    public long timestamp = 0;

    public DVSevent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(int x, int y, boolean polarity, long timestamp) {
        this.x = x;
        this.y = y;
        this.polarity = polarity;
        this.timestamp = timestamp;
    }

    /*public final static Pool<DVSEvent> EVENT_POOL = new Pool<DVSEvent>(10000, Integer.MAX_VALUE) {
        protected DVSEvent newObject() {
            return new DVSEvent();
        }
    };*/


    public void reset() {
        set(0, 0, true, 0);
    }
}
