package common;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public Logger(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String currentDateTime = dtf.format(now);

        String fileName = "java-log-" + currentDateTime + ".log";
        File logFile = new File(fileName);


    }
}
