package com.rgbtape.app;

public class TcpPacketContainer {
    public static final byte END_PACKET = -1; //equivalent to 0xff
    public static final byte RGB_PACKET = 0;
    public static final byte SHUTDOWN_PACKET = 1;
    public static final byte REBOOT_PACKET = 2;

    private byte[] packet;
    public TcpPacketContainer(byte[] packet){
        this.packet = packet;
    }

    public byte[] getPacket(){
        return this.packet;
    }
}
