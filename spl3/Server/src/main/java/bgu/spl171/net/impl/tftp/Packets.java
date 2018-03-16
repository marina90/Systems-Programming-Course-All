package bgu.spl171.net.impl.tftp;

import java.util.Optional;

/**
 * Created by Marina.Izmailov on 1/11/2017.
 */

public abstract class Packets<T> {
    boolean isRemains;
    short code;
    Optional<T> message;

    Packets(short code, Optional<T> message) {
        this.code = code;
        this.message = message;
    }


    abstract Packets executer(int connectionID);

    public short getCode() {
        return this.code;
    }

    public Optional<T> getMessage() {
        return message;
    }

}

