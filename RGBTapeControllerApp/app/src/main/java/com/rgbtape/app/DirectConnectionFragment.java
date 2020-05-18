package com.rgbtape.app;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

public class DirectConnectionFragment extends Fragment {
    private BlockingQueue<TcpPacketContainer> packetQueue = new LinkedBlockingQueue<>();
    private boolean tcpConnectionInProgres = false;
    private boolean buttonLocked = false;
    private InetAddress deviceIp;
    private DatagramSocket socket;
    private BpmCalculator bpmCalculator;

    private ExecutorService executorService;


    //Packets will remove
    private DatagramPacket customBrighnessPacket;
    private DatagramPacket onWhite;
    private DatagramPacket onBlue;
    private DatagramPacket offFade;
    private DatagramPacket off;
    byte[] customBrightness;

    private Timer timer;
    private Timer scheduleTimer;
    public static long RESET_DURATION = 2000;

    private static final String IF_CONFIG = "ifconfig wlan0";
    private static final String DEFAULT_BROADCAST_ADDRESS = "192.168.1.255";
    private static final int SOCKET_TIME_OUT = 500;
    private static final byte UDP_VERSION = 1;
    private static final int UDP_INIT_PORT = 5558;
    private static final int UDP_PORT = 5557;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tcp_direct, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();

        View view = getView();
        if (view != null){
            bpmCalculator = new BpmCalculator();
            initConnectDisconnectButtons(view);
            initShutdownRebootButtons(view);
            initFlashButtons(view);
        }
    }

    private void establishDeviceIp(){
        try {

            String broadCastAddressString = getBroadcastAddressLinux();
            InetAddress broadcastAddress;

            if (broadCastAddressString != null) {
                broadcastAddress = InetAddress.getByName(broadCastAddressString);
            } else {
                String message = "Using default broadcast address: 192.168.1.255";
                broadcastAddress = InetAddress.getByName(DEFAULT_BROADCAST_ADDRESS);
            }

            if (socket == null) {
                socket = new DatagramSocket(UDP_PORT);
                socket.setSoTimeout(SOCKET_TIME_OUT);
            }

            byte[] buf = "rupertrgbtape".getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastAddress, UDP_INIT_PORT);
            socket.send(packet);

            buf = new byte[1];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            if (packet.getData()[0] == UDP_VERSION) {
                deviceIp = packet.getAddress();
                initialisePackets();
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void closeConnection(){
        byte[] closeConnection = new byte[8];
        closeConnection[0] = 100;
        try {
            if (deviceIp != null && socket != null)
                socket.send(new DatagramPacket(closeConnection, closeConnection.length, deviceIp, UDP_PORT));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void initFlashButtons(View view){
        final Button tapButton = view.findViewById(R.id.tap_button);
        tapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTouch();
                if (bpmCalculator.times.size() >= 2){
                    flashAtFixedRate(bpmCalculator.getDelay());
                } else {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            pulseWhite();
                        }
                    });
                }
            }
        });

        Button cancelButton = view.findViewById(R.id.cancel_flashing);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelFlashing();
                String tap = "TAP";
                tapButton.setText(tap);
            }
        });

        final Button flashBlue = view.findViewById(R.id.flash_blue);
        flashBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        pulseBlue();
                    }
                });
            }
        });
    }

    private void initConnectDisconnectButtons(View view){
        Button takeOverButton = view.findViewById(R.id.takeOverButton);
        Button endConnectionButton = view.findViewById(R.id.endConnection);

        takeOverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            establishDeviceIp();
                        }
                    });
            }
        });

        endConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        closeConnection();
                    }
                });
            }
        });
    }

    private void initShutdownRebootButtons(View view){
            Button shutdownButton = view.findViewById(R.id.shutdownButton);
            Button rebootButton = view.findViewById(R.id.rebootButton);

            shutdownButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    byte[] b ={TcpPacketContainer.SHUTDOWN_PACKET,0,0,0,0};
              //      addToPacketQueue(b);
                }
            });

        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] b ={TcpPacketContainer.REBOOT_PACKET,0,0,0,0};
              //  addToPacketQueue(b);
            }
        });
    }

    private String getBroadcastAddressLinux() throws BroadcastAddressNotFoundException {
        try {
            Process process = Runtime.getRuntime().exec(IF_CONFIG);
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String match = matchBroadcastAddress(line);
                    if (match != null)
                        return match;
                }
            }
            throw new BroadcastAddressNotFoundException("No match");
        } catch (IOException e){
            throw new BroadcastAddressNotFoundException(e.getMessage());
        }
    }

    private String matchBroadcastAddress(String line){
        Pattern pattern = Pattern.compile("(broadcast )|(Bcast:)(\\d\\d?\\d?\\.){3}\\d\\d?\\d?");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()){
            String withPrefix = matcher.group();

            //Now just get IP
            pattern = Pattern.compile("(\\d\\d?\\d?\\.){3}\\d\\d?\\d?");
            matcher = pattern.matcher(withPrefix);
            if (matcher.find())
                return matcher.group();
        }
        return null;
    }

    private void handleTouch(){
        bpmCalculator.recordTime();
        restartResetTimer();
        updateView();
    }

    private void restartResetTimer(){
        stopResetTimer();
        startResetTimer();
    }

    private void stopResetTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void startResetTimer() {
        timer = new Timer("reset-bpm-calculator", true);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                bpmCalculator.clearTimes();
            }
        }, RESET_DURATION);
    }

    private void updateView(){
        Button b = getView().findViewById(R.id.tap_button);
        if (bpmCalculator.times.size() >= 2){
            int bpm = bpmCalculator.getBpm();
            b.setText(Integer.valueOf(bpm).toString());
        } else {
            String tap = "tap";
            b.setText(tap);
        }
    }

    private void flashAtFixedRate(long milli){
        if (scheduleTimer != null) {
            scheduleTimer.cancel();
        }
        scheduleTimer = new Timer();
        scheduleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                pulseWhite();
            }
        }, 0, milli );
    }

    private void cancelFlashing(){
        if (scheduleTimer != null){
            scheduleTimer.cancel();
        }
    }

    private void pulseBlue(){
        try {
            socket.send(onBlue);
            socket.send(offFade);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void pulseWhite(){
        try {
            socket.send(onWhite);
            socket.send(offFade);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void flashWhite(){
        try {
            socket.send(onWhite);
            Thread.sleep(20);
            socket.send(off);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initialisePackets(){
        byte[] onWhite = new byte[8];
        onWhite[1] = (byte) 255;
        onWhite[2] = (byte) 255;
        onWhite[3] = (byte) 255;

        byte[] onBlue = new byte[8];
        onBlue[3] = (byte) 255;

        byte[] offFade = new byte[8];
        System.arraycopy(ByteBuffer.allocate(4).putFloat(0.25f).array(),0,offFade,4,4);

        byte[] offSnap = new byte[8];

        customBrightness = new byte[8];

        this.onWhite = new DatagramPacket(onWhite,onWhite.length,deviceIp,UDP_PORT);
        this.onBlue = new DatagramPacket(onBlue,onWhite.length,deviceIp,UDP_PORT);
        this.offFade = new DatagramPacket(offFade,onWhite.length,deviceIp,UDP_PORT);
        this.off = new DatagramPacket(offSnap,onWhite.length,deviceIp,UDP_PORT);
        this.customBrighnessPacket = new DatagramPacket(customBrightness,onBlue.length,deviceIp,UDP_PORT);
    }

    class BroadcastAddressNotFoundException extends IOException{
        public BroadcastAddressNotFoundException(String message) {
            super("Broadcast address could not be found; " + message);
        }
    }
}
