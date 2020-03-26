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

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import static android.content.ContentValues.TAG;

public class TcpDirectFragment extends Fragment {
    private BlockingQueue<TcpPacketContainer> packetQueue = new LinkedBlockingQueue<>();
    private boolean tcpConnectionInProgres = false;
    private boolean buttonLocked = false;
    private ServerSocket ss;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tcp_direct, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        if (view != null) {
            initTcpConnectButton(view);
            initShutdownRebootButtons(view);
        }
    }

    private synchronized void setTcpConnectionInProgres(boolean tcpConnectionInProgres){
        this.tcpConnectionInProgres = tcpConnectionInProgres;
    }

    private synchronized boolean isTcpConnectionInProgres(){
        return this.tcpConnectionInProgres;
    }

    private synchronized void setButtonLocked(boolean buttonLocked){
        this.buttonLocked = buttonLocked;
    }

    private synchronized boolean isButtonLocked(){
        return this.buttonLocked;
    }

    private void initTcpConnectButton(View view){
        final Switch tcpConnectSwitch = view.findViewById(R.id.tcpConnectionButton);
        tcpConnectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
              //  if (!isButtonLocked()) {
                  //  setButtonLocked(true);
                    tcpConnectSwitch.setClickable(false);
                    if (isChecked) {
                        if (!isTcpConnectionInProgres()) {
                            (new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    startTcpConnection();
                                }
                            })).start();
                        } else {
                            tcpConnectSwitch.setChecked(false);
                            tcpConnectSwitch.setClickable(true);
                        }
                    } else {
                        if (isTcpConnectionInProgres()) {
                            byte[] b = {-1, 0, 0, 0, 0};
                            addToPacketQueue(b);
                        }
                    }
//                } else {
//                    tcpConnectSwitch.setChecked(!isChecked);
//                }
            }
        });
    }

    private void startTcpConnection(){
        sendUdpBroadCastPacket();
        try{
            ss = new ServerSocket(5558);
            Socket client = ss.accept();
            System.out.println("Accepted client...");
            setTcpConnectionInProgres(true);
            Button tcpConnectSwitch = getView().findViewById(R.id.tcpConnectionButton);
            tcpConnectSwitch.setClickable(true);

            OutputStream outputStream = client.getOutputStream();

            while (isTcpConnectionInProgres()){
                byte[] outByte = packetQueue.take().getPacket();
                outputStream.write(outByte);
                if (outByte[0] == TcpPacketContainer.END_PACKET){
                    ss.close();


                    setTcpConnectionInProgres(false);
                    tcpConnectSwitch.setClickable(true);
                }
                System.out.println("Wrote packet...");
            }

        }catch(Exception e) {
            Log.e(TAG,"Error reading queue", e);
            setTcpConnectionInProgres(false);
        }
    }

    private boolean addToPacketQueue(byte[] packet){
        if (isTcpConnectionInProgres()){
            try {
                packetQueue.put(new TcpPacketContainer(packet));
                return true;
            } catch (InterruptedException e){
                Log.e(TAG,"Error adding to queue", e);
            }
        }
        return false;
    }

    private void sendUdpBroadCastPacket(){
        try {
            InetAddress address = InetAddress.getByName("192.168.1.255");
            DatagramSocket socket = new DatagramSocket();

            byte[] buf = "rupertrgbtape".getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5557);
            socket.send(packet);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initShutdownRebootButtons(View view){
            Button shutdownButton = view.findViewById(R.id.shutdownButton);
            Button rebootButton = view.findViewById(R.id.rebootButton);

            shutdownButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    byte[] b ={TcpPacketContainer.SHUTDOWN_PACKET,0,0,0,0};
                    addToPacketQueue(b);
                }
            });

        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] b ={TcpPacketContainer.REBOOT_PACKET,0,0,0,0};
                addToPacketQueue(b);
            }
        });
    }
}
