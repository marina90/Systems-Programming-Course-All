package bgu.spl171.net.impl.tftp;

import java.util.Optional;

/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class Disc extends Packets {

    public Disc(){
        super((short) 10, Optional.empty());

    }
    @Override
    Packets executer(int connectionID) {
        return new Ack((short)0);
    }
}
