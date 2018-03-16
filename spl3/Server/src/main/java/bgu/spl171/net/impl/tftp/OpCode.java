package bgu.spl171.net.impl.tftp;

/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class OpCode {

    enum opCode {
        PLACEHOLDER,
        RRQ, // Read Request
        WQR, //Write Request
        DATA, //Data
        ACK, //Acknowledgement
        ERROR, //TODO make sure no error in java
        DIRQ, //Directory Listing Request
        LOGRQ, // Login Request
        DELRQ, //Delete Request
        BCAST, //Broadcast
        DISC, //Disconnect
        INVALID;

    }

    public static opCode setOpCode (short code){
        switch (code){
            case 0:
                return opCode.INVALID;
            case 1:
                return opCode.RRQ;
            case 2:
                return opCode.WQR;
            case 3:
                return opCode.DATA;
            case 4:
                return opCode.ACK;
            case 5:
                return opCode.ERROR;
            case 6:
                return opCode.DIRQ;
            case 7:
                return opCode.LOGRQ;
            case 8:
                return opCode.DELRQ;
            case 9:
                return opCode.BCAST;
            case 10:
                return opCode.DISC;
        }
        return null;
    }
}

