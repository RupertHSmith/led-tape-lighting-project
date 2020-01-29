package common;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class UIDWriter {
    public static void main(String[] args){
        UIDWriter uidWriter = new UIDWriter("VLUaKArgEQ2oENfZs9WE", "RGB Tape Controller");
    }

    public UIDWriter(String uid, String description){
        //WRITE SERIAL DATA TO DEVICE
        try {
                DeviceUID d = new DeviceUID(uid, description);
                File f  = new File("device.uid");
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
                oos.writeObject(d);
        } catch (DeviceUID.InvalidUIDException | IOException e) {
            e.printStackTrace();
        }
    }
}
