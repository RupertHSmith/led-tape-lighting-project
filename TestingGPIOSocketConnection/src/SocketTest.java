import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketTest {
    public static final int _PI_CMD_PFS = 7;


    public static void main(String args[]){
      //  try {
            //InetAddress localHost = InetAddress.getLocalHost();
            //Socket socket = new Socket(localHost, 8888);

          //  socket.setTcpNoDelay(true);
        //    OutputStream out = socket.getOutputStream();
      //      DataOutputStream dos = new DataOutputStream(out);

            //write to port 8888

            ByteBuffer b = ByteBuffer.allocate(16);

            b.putInt(5);
            b.putInt(22);
            b.putInt(200);
            b.putInt(0);


            System.out.println(Integer.reverseBytes(24));

            byte[] bytes = b.array();
            System.out.println("WRITING BYTES...");
            for (int i = 0; i < 16; i++)
                System.out.printf("0x%02X\n", bytes[i]);

        //dos.write(bytes);






       // } catch (IOException e){
          //  e.printStackTrace();
        //}
    }
}
