package bgu.spl171.net.impl.tftp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Optional;

import static bgu.spl171.net.impl.tftp.TFTPserver.dir;

/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class Dirq extends Packets {

    Dirq() {
        super((short) 6, Optional.empty());
    }

    @Override
    Packets executer(int connectionID) {
        String dirFiles = "";
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toRealPath());
            for (Path entry : stream) {
                dirFiles += (entry.getFileName().toString() + '\0');
            }

            return new Data(dirFiles.length(), (short) 0, dirFiles.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Error((short) 1);
    }
}
