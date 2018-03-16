package bgu.spl171.net.impl.tftp;

import bgu.spl171.net.api.MessageEncoderDecoder;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class TFTPMessageEncoderDecoder implements MessageEncoderDecoder<Packets> {
    private final ByteBuffer lengthBuffer = ByteBuffer.allocate(2);
    private ByteBuffer ackBuffer = ByteBuffer.allocate(2);
    private final ByteBuffer objectBytes = ByteBuffer.allocate(512);
    private ByteBuffer dataPacketHeader = ByteBuffer.allocate(4);
    private ByteBuffer writeBytes;
    private ArrayList<Byte> dataBufferTillFinish = new ArrayList<>();
    private short code;
    private short size;
    private Write tmp;
    private short blockend;

    @Override
    public Packets decodeNextByte(byte nextByte) {
        if (lengthBuffer.hasRemaining()) {
            lengthBuffer.put(nextByte);
            if (!lengthBuffer.hasRemaining()) {//check for two cases of nothing else
                code = bytesToShort(lengthBuffer.array());
                switch (code) {
                    case 6://Dirg
                        cleanup();
                        return new Dirq();
                    case 10://disc
                        cleanup();
                        return new Disc();
                    default:
                        return null;
                }
            }
        }
        if (!lengthBuffer.hasRemaining()) {
            code = bytesToShort(lengthBuffer.array());

            switch (code) {
                //Case 6 and 10 are handled separately
                case 1://read file
                    if (!isFinished(nextByte)) {
                        return null;
                    } else {
                        String Rname = ByteBufferNullterminatedStringToString(objectBytes);
                        cleanup();
                        return new Read(Rname);
                    }
                case 2://write file
                    if (!isFinished(nextByte)) {
                        return null;
                    } else {
                        String Wname = ByteBufferNullterminatedStringToString(objectBytes);
                        tmp = new Write(Wname);
                        cleanup();
                        if (tmp.isFileExist()) {
                            return new Error((short) 1);
                        } else {
                            Ack x = new Ack((short) 0);
                            x.toClient = true;
                            return x;
                        }
                    }
                case 3: //DATA
                    if (dataPacketHeader.hasRemaining()) {
                        dataPacketHeader.put(nextByte);
                        if (!dataPacketHeader.hasRemaining()) {
                            size = bytesToShort(Arrays.copyOfRange(dataPacketHeader.array(), 0, 2));
                            blockend = bytesToShort(Arrays.copyOfRange(dataPacketHeader.array(), 2, 4));
                            writeBytes = ByteBuffer.allocate(size);
                            if (size==0) {
                                dataPacketHeader.clear();
                                cleanup();
                                tmp.fileWrite(dataBufferTillFinish);
                                tmp.message = Optional.of(dataBufferTillFinish);
                                tmp.finalBlockNumber = blockend;
                                return tmp;
                            }
                        }
                        return null;
                    }
                    writeBytes.put(nextByte);
                    if (writeBytes.hasRemaining()) {
                        return null;
                    }
                    for (byte x : writeBytes.array()) {
                        dataBufferTillFinish.add(x);
                    }
                    dataPacketHeader.clear();
                    cleanup();
                    //if less than 512, finished reading dataPacketHeader from client
                    if (size < 512) {
                        tmp.fileWrite(dataBufferTillFinish);
                        tmp.message = Optional.of(dataBufferTillFinish);
                        dataBufferTillFinish.clear();
                        writeBytes.clear();
                        tmp.finalBlockNumber = blockend;
                        return tmp;
                    } else { //if 512, need to send ack and wait more for more dataPacketHeader
                        //reinit
                        Ack x = new Ack(blockend);
                        x.toClient = true;
                        return x;
                    }
                case 4: //ACK
                    ackBuffer.put(nextByte);
                    if (!ackBuffer.hasRemaining()) {
                        Ack x = new Ack(bytesToShort(ackBuffer.array()));
                        cleanup();
                        ackBuffer.clear();
                        return x;
                    }
                    break; //will return null
                case 5://error

                    if (!isFinished(nextByte)) {
                        return null;
                    } else {
                        Error x = new Error(bytesToShort(objectBytes.array()));
                        cleanup();
                        return x;
                    }

                case 7://login
                    if (!isFinished(nextByte)) {
                        return null;
                    }
                    String Lname = ByteBufferNullterminatedStringToString(objectBytes);
                    cleanup();
                    return new Login(Lname);
                case 8://delete
                    if (!isFinished(nextByte)) {
                        return null;
                    }
                    String Fname = ByteBufferNullterminatedStringToString(objectBytes);
                    cleanup();
                    return new Delete(Fname);
                case 9://Bcast
                    String Bname = ByteBufferNullterminatedStringToString(objectBytes);
                    cleanup();
                    if (nextByte == 0) {
                        return new Bcast((byte) 0, Bname);
                    } else if (nextByte == 1) {
                        return new Bcast((byte) 1, Bname);
                    }
                    break;
            }

        }
        return null;
    }


    @Override
    public byte[] encode(Packets message) throws UnsupportedEncodingException {
        short code = message.getCode();
        switch (code) {
            case 0://error?
                return null;
            case 1://read file
                ByteArrayOutputStream read = new ByteArrayOutputStream();
                try {
                    read.write(shortToBytes((short) 1));
                    WriteNullTermStringToStream(read, ((String) message.getMessage().get()));
                    return read.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 2://write
                ByteArrayOutputStream write = new ByteArrayOutputStream();
                try {
                    write.write(shortToBytes((short) 2));
                    WriteNullTermStringToStream(write, ((String) message.getMessage().get()));
                    return write.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 3: //dataPacketHeader
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    Data x = (Data) message;
                    outputStream.write(shortToBytes((short) 3));
                    outputStream.write(shortToBytes((short) x.size));
                    outputStream.write(shortToBytes(x.block));
                    outputStream.write(x.messageByte);
                    return outputStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 4: //ack
                ByteArrayOutputStream ack = new ByteArrayOutputStream();
                try {
                    Ack x = (Ack) message;
                    ack.write(shortToBytes((short) 4));
                    ack.write(shortToBytes(x.indicator));
                    return ack.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 5: //Error
                ByteArrayOutputStream error = new ByteArrayOutputStream();
                try {
                    Error x = (Error) message;
                    error.write(shortToBytes((short) 5));
                    error.write(shortToBytes(x.index));
                    WriteNullTermStringToStream(error, ((String) message.getMessage().get()));
                    return error.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 6://dirq
            case 7://login
                //TODO
                return null;
            case 8: //Delete, should never happen
                ByteArrayOutputStream delete = new ByteArrayOutputStream();
                try {
                    delete.write(shortToBytes((short) 8));
                    WriteNullTermStringToStream(delete, ((String) message.getMessage().get()));
                    return delete.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            case 9: //Bcast
                ByteArrayOutputStream bcastStream = new ByteArrayOutputStream();
                try {
                    Bcast x = (Bcast) message;
                    bcastStream.write(shortToBytes((short) 9));
                    bcastStream.write(x.ind);
                    WriteNullTermStringToStream(bcastStream, ((String) message.getMessage().get()));
                    return bcastStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 10://disc
                return null;
        }
        return null;
    }

    private void WriteNullTermStringToStream(ByteArrayOutputStream stream, String toWrite) {
        try {
            stream.write(toWrite.getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        stream.write(0);
    }

    private byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    private static String ByteBufferNullterminatedStringToString(ByteBuffer x) {
        int i = 0;
        for (; i < x.capacity(); i++) {
            if (x.get(i) == 0) {
                break;
            }
        }
        byte[] finalArr = new byte[i];
        for (int j = 0; j < i; j++) {
            finalArr[j] = x.get(j);
            x.put(j, (byte) 0);
        }
        x.clear();
        return new String(finalArr, StandardCharsets.UTF_8);
    }

    private void cleanup() {
        objectBytes.clear();
        lengthBuffer.clear();
    }

    private boolean isFinished(byte nextByte) {
        if (nextByte != 0) {
            objectBytes.put(nextByte);
            return false;
        }
        return true;
    }
}

