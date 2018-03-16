package bgu.spl171.net.impl.TFTPreactor;

import bgu.spl171.net.impl.tftp.TFTPMessageEncoderDecoder;
import bgu.spl171.net.impl.tftp.TFTPprotocol;
import bgu.spl171.net.impl.tftp.TFTPserver;
import bgu.spl171.net.srv.Server;


import java.nio.file.Path;

/**
 * Created by Marina.Izmailov on 1/14/2017.
 */
public class ReactorMain {

    public static void main(String[] args){
        TFTPserver.init();
        if (args.length != 1) {
            return;
        }

        int port = Integer.parseInt(args[0]);
        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                port, //port
                TFTPprotocol::new, //protocol factory
                TFTPMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
    }


}
