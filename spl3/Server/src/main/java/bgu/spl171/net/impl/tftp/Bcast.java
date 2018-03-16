package bgu.spl171.net.impl.tftp;

import java.util.Optional;

/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class Bcast extends Packets{
    String message;
    byte ind ;
    Bcast(byte ind , String message){
        super((short)9,Optional.of(message));
        this.message = message;
        this.ind = ind;
    }

    @Override
    Packets executer(int connectionID) {

        return this;
    }
}
