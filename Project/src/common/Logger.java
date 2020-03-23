package common;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private String fileName;

    public Logger() throws IOException {
        String currentDateTime = getCurrentTime();

        String fileName = "java-log-" + currentDateTime + ".log";
        File logFile = new File(fileName);
        if(logFile.createNewFile()){
            System.out.println("Log file created");
        } else {
            throw new IOException("File exists or could not be created");
        }
    }

    private String getCurrentTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy_HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public synchronized void writeMessage(Object fromClass, String errorMessage)  {
        String tag =  "[" + getCurrentTime() + "] Message from class '" + fromClass.getClass().getSimpleName() + "':\n";
        String message = tag + errorMessage + "\n";

        if (!fileName.equals("")){
            try {
                FileWriter fileWriter = new FileWriter(fileName);
                fileWriter.write(message);
                fileWriter.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public synchronized void writeError(Object fromClass, String errorMessage)  {
        String tag =  "[" + getCurrentTime() + "] Error message from class '" + fromClass.getClass().getSimpleName() + "':\n";
        String message = tag + errorMessage + "\n";

        if (!fileName.equals("")){
            try{
                FileWriter fileWriter = new FileWriter(fileName);
                fileWriter.write(message);
                fileWriter.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public synchronized void writeError(Object fromClass, Exception e)  {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        String errorMessage = errors.toString();

        String tag =  "[" + getCurrentTime() + "] Error message from class '" + fromClass.getClass().getSimpleName() + "':\n";
        String message = tag + errorMessage + "\n";

        if (!fileName.equals("")){
            try {
                FileWriter fileWriter = new FileWriter(fileName);
                fileWriter.write(message);
                fileWriter.close();
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
}
