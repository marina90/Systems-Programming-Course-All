package bgu.spl171.net.impl.tftp;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import java.util.WeakHashMap;


/**
 * Created by Marina.Izmailov on 1/6/2017.
 */
public class TFTPprotocol implements BidiMessagingProtocol<Packets> {
    int connectionId;
    Connections<Packets> connections;
    Packets tmp = null;
    boolean logged = false;
    public static WeakHashMap<Integer, String> userName = new WeakHashMap<>();
    private boolean shouldTerminate = false;

    @Override
    public void start(int connectionId, Connections<Packets> connections) {
        this.connectionId = connectionId;
        this.connections = connections;

    }

    @Override
    public void process(Packets message) {

        handleOpCode(message.getCode(), message);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public Packets handleOpCode(short code, Packets message) {
        switch (code) {
            case 0:
                connections.send(connectionId, new Error((short) 0).executer(connectionId));
                break;
            case 1:
                checkTmp(message);
                break;
            case 2: //WRQ
                if (logged) {
                    Write msg = (Write)message;
                    Packets x = msg.executer(connectionId);
                    if (x.getCode() == (short) 9) { //Bcast
                        connections.send(connectionId, msg.buildRightAck().executer(connectionId));
                        bcast(x);
                    } else {
                        connections.send(connectionId,x);
                    }
                } else {
                    connections.send(connectionId, new Error((short) 6).executer(connectionId));
                }
                break;
            case 3: //Data
                //TODO ADD VERIFICATON BLOCK
                if (logged && !message.getMessage().equals(0) && tmp.isRemains) {
                    connections.send(connectionId, tmp.executer(connectionId));
                } else {
                    connections.send(connectionId, new Error((short) 2).executer(connectionId));
                }
                break;
            case 4: //ACK
                if (logged) {
                /*
                types of ACKs
                ACK where client received data from us.
                ACK we want to send:
                    *we* want to start communication with client (WRQ)
                    *we* want to tell the user we received data
                Those are 3 different ACKs
                 */
                    Ack msg = (Ack) message;
                    if (msg.toClient) {
                        connections.send(connectionId, message.executer(connectionId));
                    } else {
                        if((null != tmp)) {
                            connections.send(connectionId, tmp.executer(connectionId));
                            if (!tmp.isRemains) {
                                tmp = null;
                            }
                        }
                    }
                } else {
                    connections.send(connectionId, new Error((short) 6).executer(connectionId));
                }
                break;
            case 5://error
                connections.send(connectionId, message.executer(connectionId));
                break;
            case 6: //DirQ
                checkTmp(message);
                break;
            case 7: //logged in
                if (logged) {
                    connections.send(connectionId, new Error((short) 7).executer(connectionId));
                    return null;
                } else {
                    Packets x = message.executer(connectionId);
                    if (x.code == 4) { //ack success
                        logged = true;
                    }
                    connections.send(connectionId, x);
                }
                break;
            case 8:
                if (!logged) {
                    connections.send(connectionId, new Error((short) 6).executer(connectionId));
                    return null;
                } else {
                    Packets t = message.executer(connectionId);
                    if (t.getCode() == (short) 9) {
                        connections.send(connectionId,new Ack((short)0).executer(connectionId));
                        bcast(t);
                    } else {
                        connections.send(connectionId, t);
                    }
                }
                break;
            case 9:
                if (logged) {
                    bcast(message.executer(connectionId));
                } else {
                    connections.send(connectionId, new Error((short) 6).executer(connectionId));
                }
                break;
            case 10:
                if (logged) {
                    userName.remove(connectionId);
                    connections.send(connectionId, message.executer(connectionId));
                    shouldTerminate=true;
                } else {
                    connections.send(connectionId, new Error((short) 6).executer(connectionId));
                }
                break;
        }
        return null;
    }

    private void checkTmp(Packets message) {
        if (logged) {
            tmp = message.executer(connectionId);
            connections.send(connectionId, tmp.executer(connectionId));
            if (!tmp.isRemains) {
                tmp = null;
            }
        } else {
            connections.send(connectionId, new Error((short) 6).executer(connectionId));
        }
    }

    private void bcast(Packets message) {
        userName.forEach((integer, String) -> connections.send(integer, message));
    }
}
