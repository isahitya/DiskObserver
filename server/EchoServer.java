import java.net.*;
import java.io.*;

public class EchoServer extends Thread {
 
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
 
    public EchoServer(int port) throws Exception{
        socket = new DatagramSocket(port);
    }
 
    public void run() {
try
{
        running = true;
 
        while (running) {
            DatagramPacket packet 
              = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received 
              = new String(packet.getData(), 0, packet.getLength());
             System.out.println(received);
if(received.trim().equals("ping"))
{
byte []msgFromServer="pong".getBytes();
DatagramPacket sendPacket=new DatagramPacket(msgFromServer,msgFromServer.length,address,port);
socket.send(sendPacket);
}
}
socket.close();
}catch(Exception e){System.out.println(e);}
}

}