package bgu.spl171.net.impl.tftp;

import java.util.Optional;

/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class Error extends Packets {
    short index = 0;

    Error(short msg) {
        super((short) 5, Optional.of(msg));
        this.index = msg;
        switch (code) {
            case 0:
                message = Optional.of("Not defined, see error message (if any)");
            case 1:
                message = Optional.of("File not found – RRQ \\ DELRQ of non-existing file");
            case 2:
                message = Optional.of("Access violation – File cannot be written, read or deleted.");
            case 3:
                message = Optional.of("Disk full or allocation exceeded – No room in disk.");
            case 4:
                message = Optional.of("Illegal TFTP operation – Unknown Opcode.");
            case 5:
                message = Optional.of("File already exists – File name exists on WRQ.");
            case 6:
                message = Optional.of("User not logged in – Any opcode received before Login completes.");
            case 7:
                message = Optional.of("User already logged in – Login username already connected.");
        }
    }

    @Override
    Packets executer(int connectionID) {
        return new Error(index);
    }
}