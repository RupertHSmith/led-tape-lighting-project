package com.rgbtape.app;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpDirectFragment extends Fragment {
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

    private void initTcpConnectButton(View view){
        Switch tcpConnectSwitch = view.findViewById(R.id.tcpConnectionButton);
        tcpConnectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    (new Thread(new Runnable() {
                        @Override
                        public void run() {
                            attemptConnection();
                        }
                    })).start();
                }
            }
        });
    }

    private void attemptConnection(){
        sendUdpBroadCastPacket();
        try{
            ServerSocket ss = new ServerSocket(5558);
            for(;;){
                try{
                    Socket client = ss.accept();
                    System.out.println("Accepted client...");
                    OutputStream outputStream = client.getOutputStream();


                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(1000);
                        byte[] b = new byte[5];
                        b[0] = 0;
                        b[1] = 0;          //R
                        b[2] = 0;          //G
                        b[3] = (byte) 255; //B
                        b[4] = 0;
                        System.out.println("B");
                        outputStream.write(b);
                        Thread.sleep(1000);
                        b = new byte[5];
                        b[0] = 0;
                        b[1] = (byte) 255;
                        b[2] = 0;
                        b[3] = 0;
                        b[4] = 0;
                        System.out.println("R");
                        outputStream.write(b);
                        Thread.sleep(1000);
                        b = new byte[5];
                        b[0] = 0;
                        b[1] = 0;
                        b[2] = (byte) 255;
                        b[3] = 0;
                        b[4] = 0;
                        System.out.println("G");
                        outputStream.write(b);
                    }

                    byte[] b = new byte[5];
                    b[0] = 2;
                    b[1] = 0;          //R
                    b[2] = 0;          //G
                    b[3] = (byte) 255; //B
                    b[4] = 0;
                    outputStream.write(b);
/*
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));
                    String line;
                    while((line = in.readLine()) != null)
                        System.out.println(line+" received");
                    client.close();*/
                }catch(Exception e){System.out.println("error "+e);}
            }
        }catch(Exception e){System.out.println("error "+e);}
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
    }
}
