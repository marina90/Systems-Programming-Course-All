package bgu.spl171.net.impl.tftp;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class Data extends Packets {
    int size;
    short block;
    byte[] message;
    byte[] messageByte;

    Data(int size, short block, byte[] message) {
        super((short) 3, Optional.of(message));
        this.size = size;
        this.block = block;
        this.message = message;
        this.messageByte = message;
    }

    private Packets spliter() {

        if (size < 512) {
            isRemains = false;
            return new Data((short) size, ++block, messageByte);
        }
        isRemains = true;
        byte[] origin = messageByte;
        byte[] tmp = Arrays.copyOfRange(origin, 0, 512);
        byte[] remains = Arrays.copyOfRange(origin, 512, origin.length);
        size = remains.length;
        messageByte = remains;
        return new Data((short) 512, ++block, tmp);
    }

    @Override
    Packets executer(int connectionID) {
        return spliter();
    }
}
