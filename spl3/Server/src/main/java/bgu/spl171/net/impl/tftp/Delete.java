package bgu.spl171.net.impl.tftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

import static bgu.spl171.net.impl.tftp.TFTPserver.dir;

/**
 * Created by Marina.Izmailov on 1/12/2017.
 */
public class Delete extends Packets {
    String s;

    Delete(String s) {
        super((short) 8, Optional.of(s));
        this.s = s;
    }

    @Override
    Packets executer(int connectionID) {
        boolean find = Files.exists(Paths.get(dir.getFileName() + File.separator + s.trim()).toAbsolutePath().normalize());
        if (find) {
            try {
                if (Files.deleteIfExists(Paths.get(dir.getFileName() + File.separator + s.trim()).toAbsolutePath().normalize()))
                    return new Bcast((byte) 0, s.trim()); //null terminator added in encoder
            } catch (IOException e) {
                e.printStackTrace();
                return new Error((short) 1);
            }
        }
        return new Error((short) 1);
    }
}
