package bgu.spl171.net.impl.tftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Marina.Izmailov on 1/6/2017.
 */
public class TFTPserver {

    public static Path dir ;
    int connectionID = 0;
    public static void init() {
        try {
            if(Files.exists(Paths.get("Files").toAbsolutePath().normalize())){
                dir =Paths.get("Files").toAbsolutePath().normalize();
            }else {
                dir = Files.createDirectory(Paths.get("Files").toAbsolutePath().normalize());}
        } catch (IOException e) {
            e.printStackTrace();
        }
}
}
