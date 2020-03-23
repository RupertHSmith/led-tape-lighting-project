package common;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private PrintWriter printWriter;

    public Logger() throws IOException {
        String currentDateTime = getCurrentTime();

        System.out.println(currentDateTime);
        String fileName = "/home/pi/RGBProject/java-logs/java-log-" + currentDateTime + ".log";

        Path path = Paths.get(fileName);
        Files.createDirectories(path.getParent());
        Files.createDirectories(path.getParent());
        Files.createFile(path);

        FileWriter fileWriter = new FileWriter(fileName, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        printWriter = new PrintWriter(bufferedWriter);
    }

    private String getCurrentTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public synchronized void writeMessage(Object fromClass, String errorMessage)  {
        String tag =  "[" + getCurrentTime() + "] Message from class '" + fromClass.getClass().getSimpleName() + "': ";
        String message = tag + errorMessage;
        printWriter.println(message);
    }

    public synchronized void writeError(Object fromClass, String errorMessage)  {
        String tag =  "[" + getCurrentTime() + "] Error message from class '" + fromClass.getClass().getSimpleName() + "': ";
        String message = tag + errorMessage;
        printWriter.println(message);
    }


    public synchronized void writeError(Object fromClass, Exception e)  {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        String errorMessage = errors.toString();

        String tag =  "[" + getCurrentTime() + "] Error message from class '" + fromClass.getClass().getSimpleName() + "':\n";
        String message = tag + errorMessage;
        printWriter.println(message);
    }


}
