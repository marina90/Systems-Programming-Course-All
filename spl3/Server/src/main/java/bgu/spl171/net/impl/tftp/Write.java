package bgu.spl171.net.impl.tftp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import static bgu.spl171.net.impl.tftp.TFTPserver.dir;

/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class Write extends Packets {
    String fileName;
    boolean finished;
    Path path;
    short finalBlockNumber = 0;


    Write(String fileName) {
        super((short) 2, Optional.of(fileName));
        this.fileName = fileName;
    }

    void fileWrite(ArrayList<Byte> msg) {
        path = Paths.get(dir.toAbsolutePath() + File.separator + fileName.trim());
        try {
            BufferedWriter writer = Files.newBufferedWriter(path);
            msg.forEach((x) -> {
                try {
                    writer.write(x);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.close();
            finished = true;
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    boolean isFileExist() {
        if (Files.exists(Paths.get(dir.toAbsolutePath() + File.separator + fileName.trim()))) {
            return true;
        }
        return false;
    }

    @Override
    Packets executer(int connectionID) {

        if (!finished) {
            return new Error((short) 5);
        }

        return new Bcast((byte) 1, fileName);
    }

    Ack buildRightAck() {
        return new Ack(finalBlockNumber);
    }
}
