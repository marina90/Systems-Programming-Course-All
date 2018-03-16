package bgu.spl171.net.impl.tftp;

import java.util.Optional;

import static bgu.spl171.net.impl.tftp.TFTPprotocol.userName;


/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class Login extends Packets {
    String message;

    Login(String message) {
        super((short) 7, Optional.of(message));
        this.message = message;
    }

    @Override
    Packets executer(int connectionId) {
        synchronized (userName) {
            if (!userName.containsValue(message)) {
                userName.put(connectionId, message);
            } else {
                return new Error((short) 7);
            }
            return new Ack((short) 0);
        }
    }
}
