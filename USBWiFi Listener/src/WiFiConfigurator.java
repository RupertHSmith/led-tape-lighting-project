import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WiFiConfigurator {
    public static void addWiFiNetwork(String wifiEntry){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/etc/wpa_supplicant/wpa_supplicant.conf", true));
            writer.newLine();
            writer.write(wifiEntry);
            System.out.println("Wrote new WiFi entry");
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
