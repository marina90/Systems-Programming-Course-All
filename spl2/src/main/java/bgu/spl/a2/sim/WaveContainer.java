package bgu.spl.a2.sim;

import java.util.ArrayList;

/**
 * Created by ace1_ on 29-Dec-16.
 */
class WaveContainer {
    ArrayList<OrderContainer> orders;
    public int numOrdersFinal = 0;

    WaveContainer(int numOrders) {
        orders = new ArrayList(numOrders);
    }

}
