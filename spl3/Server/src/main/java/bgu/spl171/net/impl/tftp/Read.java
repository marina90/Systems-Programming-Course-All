package bgu.spl171.net.impl.tftp;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Created by Marina.Izmailov on 1/14/2017.
 */
public class Read extends Packets {
    String fileName;
    String encoding;
    Read(String message) {
        super((short) 1, Optional.of(message));
        this.fileName = message;

    }

    private byte[] readFiles() {
        if (Files.exists(Paths.get(TFTPserver.dir.getFileName() + File.separator + fileName.trim()).toAbsolutePath().normalize())) {

            Path file = Paths.get(TFTPserver.dir.getFileName() + File.separator + fileName.trim()).toAbsolutePath().normalize();
            byte[] result = read(file.toString());
            return result;
        }
        return null;
    }

    byte[] read(String aInputFileName){
        File file = new File(aInputFileName);
        byte[] result = new byte[(int)file.length()];
        try {
            InputStream input = null;
            try {
                int totalBytesRead = 0;
                input = new BufferedInputStream(new FileInputStream(file));
                while (totalBytesRead < result.length) {
                    int bytesRemaining = result.length - totalBytesRead;
                    //input.read() returns -1, 0, or more :
                    int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                    if (bytesRead > 0){
                        totalBytesRead = totalBytesRead + bytesRead;
                    }
                }
            }
            finally {
                input.close();
            }
        }
        catch (IOException ex) {
            System.out.println("caught exception: " + ex);
        }
        return result;
    }
    @Override
    Packets executer(int connectionID) {
        byte[] file = readFiles();
        if (file != null) {
            return new Data(file.length, (short) 0, file);
        } else {
            return new Error((short) 1);
        }
    }
}
