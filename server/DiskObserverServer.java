import java.io.*;
import java.util.*;
import java.net.*;
public class DiskObserverServer
{
public static void main(String gg[])
{
try
{
if(gg.length==0)
{
System.out.println("Usage : java DiskObserverServer [port]");
return;
}
int port=Integer.parseInt(gg[0]);
SocketCommunicator.serverSide=true;
EchoServer es=new EchoServer(port);
es.start();
ServerSocket server=new ServerSocket(port);
while(true)
{
Socket client=server.accept();
System.out.println("Connection ");
FolderBackupServer fbs=new FolderBackupServer(client);
fbs.start();
}
}catch(Exception e){
System.out.println(e);
}
}
}