package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.impl.tftp.TFTPMessageEncoderDecoder;
import bgu.spl171.net.impl.tftp.TFTPprotocol;
import bgu.spl171.net.impl.tftp.TFTPserver;
import bgu.spl171.net.srv.Server;


import java.nio.file.Path;

/**
 * Created by Marina.Izmailov on 1/14/2017.
 */
public class TPCMain {

    public static void main(String[] args){
        TFTPserver.init();
        if (args.length != 1) {
            return;
        }
        int port = Integer.parseInt(args[0]);
        Server.threadPerClient(
                port, //port
                TFTPprotocol::new, //protocol factory
                TFTPMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
    }
}
