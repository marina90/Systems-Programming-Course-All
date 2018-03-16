package bgu.spl171.net.impl.tftp;

import java.util.Optional;

/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class Ack extends Packets {
    short code;
    short indicator;
    boolean toClient = false;
    Ack(short indicator) {
        super((short)4, Optional.of(indicator));
        this.indicator =indicator;

    }
    @Override

    Packets executer(int connectionId) {
        return this;
    }
}
