import net.samuelcampos.usbdrivedetector.USBDeviceDetectorManager;
import net.samuelcampos.usbdrivedetector.USBStorageDevice;
import net.samuelcampos.usbdrivedetector.events.DeviceEventType;
import net.samuelcampos.usbdrivedetector.events.IUSBDriveListener;
import net.samuelcampos.usbdrivedetector.events.USBStorageEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class USBListener implements IUSBDriveListener {
    public static void main (String[] args){
        USBListener listener = new USBListener();
        USBDeviceDetectorManager usbDeviceDetectorManager = new USBDeviceDetectorManager();
        usbDeviceDetectorManager.addDriveListener(listener);

        while (true){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void usbDriveEvent(USBStorageEvent usbStorageEvent) {
        System.out.println("Received USB event...");
        processDevice(usbStorageEvent.getStorageDevice());

        //Now we shut down the device...
        Runtime p = Runtime.getRuntime();
        try {
            p.exec("sudo shutdown");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void processDevice(USBStorageDevice device){
        if (device != null){
            System.out.println("Reading device: " + device.getDeviceName());
            System.out.println("Device directory: " + device.getRootDirectory().getAbsolutePath());

            String filePath = device.getRootDirectory().getAbsolutePath() + "/wifi_entry.wifi";
            File fileToFind = new File(filePath);
            if (fileToFind.isFile()){
                String wifiFile = readAllBytesJava7(filePath);
                if (wifiFile.length() > 0){
                    WiFiConfigurator.addWiFiNetwork(wifiFile);
                }
            } else {
                System.err.println("Invalid WiFi file...");
            }
        } else {
            System.err.println("Invalid usb device!");
        }
    }

    private static String readAllBytesJava7(String filePath)
    {
        String content = "";
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return content;
    }
}
