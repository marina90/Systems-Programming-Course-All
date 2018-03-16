package bgu.spl171.net.srv;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.bidi.ConnectionHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;

/**
 * Created by Marina.Izmailov on 1/6/2017.
 */
public class ConnectionsImpl<T> implements Connections<T> {

    private WeakHashMap<Integer,ConnectionHandler<T>> storeIDs= new WeakHashMap<>();

    public void add(int connectionId,ConnectionHandler<T> t)  {
        storeIDs.put(connectionId,t);
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if (storeIDs.get(connectionId) != null){
            storeIDs.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {

        storeIDs.forEach((integer, tConnectionHandler) -> send(integer,msg));
        }

    @Override
    public void disconnect(int connectionId) {
        try {
            storeIDs.get(connectionId).close();
            storeIDs.remove(connectionId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

