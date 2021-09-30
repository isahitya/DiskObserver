import java.net.*;
import java.util.*;
import java.io.*;
public class SocketCommunicator
{
private static int SendObject=1;
private static int RecvObject=2;
private static int SendFile=3;
private static int RecvFile=4;
public static boolean serverSide=false;
public static File desFolder=null;
public static long size;
public static long read;
public static void sendObject(Socket socket,Serializable obj)
{
try
{
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
byte[] ack=new byte[1];
int byteCount;
ByteArrayOutputStream baos=new ByteArrayOutputStream();
ObjectOutputStream oos=new ObjectOutputStream(baos);
oos.writeObject(obj);
oos.flush();
byte[] bytes=baos.toByteArray();
int numberOfBytes=bytes.length;
//os.write(SendObject);
//is.read(ack);
//if(ack[0]!=77)throw new RuntimeException("Can't receive acknowledgement");
byte b[]=new byte[4];
b[0]=(byte)(numberOfBytes>>24);
b[1]=(byte)(numberOfBytes>>16);
b[2]=(byte)(numberOfBytes>>8);
b[3]=(byte)numberOfBytes;
os.write(b);
os.flush();
is.read(ack);
if(ack[0]!=77)throw new RuntimeException("Can't receive acknowledgement");
int bytesToSend=numberOfBytes;
int chunkSize=1024;
int off=0;
//System.out.println("Bytes to send"+bytesToSend);
while(bytesToSend>0)
{
if(bytesToSend<chunkSize)chunkSize=bytesToSend;
os.write(bytes,off,chunkSize);
os.flush();
is.read(ack);
if(ack[0]!=77)throw new RuntimeException("Can't receive acknowledgement");
off+=chunkSize;
bytesToSend-=chunkSize;
}
is.read(ack);
if(ack[0]!=77)throw new RuntimeException("Can't receive acknowledgement");
//System.out.println("Object sent");
}catch(Exception e){
//System.out.println(e);
}
}//send object
public static Object recvObject(Socket socket,InputStream inputStream) throws IOException
{
try
{
int byteCount;
InputStream is=inputStream;
ByteArrayOutputStream baos=null;
byte[] ack=new byte[1];
OutputStream os=socket.getOutputStream();
//os.write((byte)77);
//os.flush();
byte [] objectLengthInBytes=new byte[4];
byteCount=is.read(objectLengthInBytes);
int objectLength;
objectLength=(objectLengthInBytes[0] & 0xFF) << 24 | (objectLengthInBytes[1] & 0xFF) <<
16 | (objectLengthInBytes[2] & 0xFF) << 8 | (objectLengthInBytes[3] & 0xFF);
ack[0]=77;
os.write(ack,0,1);
os.flush();
baos=new ByteArrayOutputStream();
byte chunk[]=new byte[1024];
int bytesToRead=objectLength;
//System.out.println("Bytes to read "+bytesToRead);
while(bytesToRead>0)
{
byteCount=is.read(chunk);
os.write(ack,0,1);
os.flush();
if(byteCount>0)
{
baos.write(chunk,0,byteCount);
baos.flush();
}
bytesToRead-=byteCount;
}
os.write(ack,0,1);
os.flush();
byte responseBytes[]=baos.toByteArray();
ByteArrayInputStream bais=new ByteArrayInputStream(responseBytes);
ObjectInputStream ois=new ObjectInputStream(bais);
//System.out.println("Object received");
return ois.readObject();
}catch(IOException ioe){
throw ioe;
}
catch(Exception e)
{
//System.out.println(e);
}
return null;
}//recv object

private static boolean isCompletelyWritten(File file) {
    RandomAccessFile stream = null;
    try {
        stream = new RandomAccessFile(file, "rw");
        return true;
    } catch (Exception e) {
        //log.info("Skipping file " + file.getName() + " for this iteration due it's not completely written");
    } finally {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                //log.error("Exception during closing file " + file.getName());
            }
        }
    }
    return false;
}

public static void sendFile(Socket socket,FilePojo fileToSendPojo)
{
FileInputStream fis=null;
try
{
File fileToSend=null;
if(serverSide)fileToSend=fileToSendPojo.serverFile;
else fileToSend=fileToSendPojo.clientFile;
if(fileToSend.isDirectory())
{
//System.out.println("File to send is a directory");
return;
}
while(!isCompletelyWritten(fileToSend))
{
    Thread.sleep(500);
}
ClientPanel.fileTransferLabel.setText("File :"+fileToSend.getName());
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
fis=new FileInputStream(fileToSend);
byte[] ack=new byte[1];
int byteCount;
long numberOfBytes=fileToSend.length();
//os.write(SendFile);
//is.read(ack);
//if(ack[0]!=77)throw new RuntimeException("Can't receive acknowledgement");
byte b[]=new byte[8];
b[0]=(byte)(numberOfBytes>>56);
b[1]=(byte)(numberOfBytes>>48);
b[2]=(byte)(numberOfBytes>>40);
b[3]=(byte)(numberOfBytes>>32);
b[4]=(byte)(numberOfBytes>>24);
b[5]=(byte)(numberOfBytes>>16);
b[6]=(byte)(numberOfBytes>>8);
b[7]=(byte)numberOfBytes;
os.write(b);
os.flush();
is.read(ack);
if(ack[0]!=77)throw new RuntimeException("Can't receive acknowledgement");
long bytesToSend=numberOfBytes;
int chunkSize=1024;
byte[] bytes=new byte[chunkSize];
int off=0;
int val=0;
while(bytesToSend>0)
{
if(bytesToSend<chunkSize)chunkSize=(int)bytesToSend;
fis.read(bytes,0,chunkSize);
os.write(bytes,0,chunkSize);
SocketCommunicator.read+=chunkSize;
val=(int)(((double)SocketCommunicator.read/(double)SocketCommunicator.size)*100);
ClientPanel.transferProgress.setValue(val);
//System.out.println(val);
os.flush();
is.read(ack);
if(ack[0]!=77)throw new RuntimeException("Can't receive acknowledgement");
off+=chunkSize;
bytesToSend-=chunkSize;
}
is.read(ack);
if(ack[0]!=77)throw new RuntimeException("Can't receive acknowledgement");
//System.out.println("File sent : "+fileToSend.getName());
fis.close();
}catch(Exception e){
//System.out.println(e);
if(fis!=null)try
{
fis.close();
}catch(IOException ioe){
//System.out.println(ioe);
}
}
}//send file
public static void recvFile(Socket socket,InputStream inputStream,FilePojo fileToSavePojo)
{
FileOutputStream fos=null;
try
{
File fileToSave=null;
if(serverSide)fileToSave=fileToSavePojo.serverFile;
else fileToSave=fileToSavePojo.clientFile;
if(fileToSave.exists())fileToSave.delete();
//System.out.println("idhr ayayyy1");
File p=fileToSave.getParentFile();
if(p!=null)
{
try
{
//System.out.println("parent "+p.getName());
if(p.isDirectory()==false)p.delete();
p.mkdirs();
}catch(Exception e){
//System.out.println(e);
}
//System.out.println("Parent file:"+fileToSave.getParentFile());
}
//System.out.println("idhr aya2");
fileToSave.createNewFile();
//System.out.println("idhr ayayyy3");
fos=new FileOutputStream(fileToSave);
int byteCount;
InputStream is=inputStream;
ByteArrayOutputStream baos=null;
byte[] ack=new byte[1];
OutputStream os=socket.getOutputStream();
//os.write((byte)77);
//os.flush();
byte [] objectLengthInBytes=new byte[8];
byteCount=is.read(objectLengthInBytes);
int objectLength;
objectLength=(objectLengthInBytes[0] & 0xFF) << 56 | (objectLengthInBytes[1] & 0xFF) <<
48 | (objectLengthInBytes[2] & 0xFF) << 40 | (objectLengthInBytes[3] & 0xFF) << 32 | (objectLengthInBytes[4] & 0xFF) << 24 | (objectLengthInBytes[5] & 0xFF) << 16 | (objectLengthInBytes[6] & 0xFF) << 8 | (objectLengthInBytes[7] & 0xFF);
ack[0]=77;
os.write(ack,0,1);
os.flush();
baos=new ByteArrayOutputStream();
byte chunk[]=new byte[1024];
int bytesToRead=objectLength;
while(bytesToRead>0)
{
byteCount=is.read(chunk);
os.write(ack,0,1);
os.flush();
if(byteCount>0)
{
fos.write(chunk,0,byteCount);
}
bytesToRead-=byteCount;
}
os.write(ack,0,1);
os.flush();
fos.close();
}catch(Exception e)
{
//System.out.println(e);
if(fos!=null)try{
fos.close();
}catch(IOException ioe){
    //System.out.println(ioe);
}
}
}//recv file
public static void recvFileTo(Socket socket,InputStream inputStream,FilePojo fileToSavePojo)
{
FileOutputStream fos=null;
try
{
File toFolder=desFolder;
//System.out.println("Destination folder "+toFolder.getCanonicalPath());
File fileToSave=null;
if(serverSide)fileToSave=fileToSavePojo.serverFile;
else fileToSave=fileToSavePojo.clientFile;
fileToSave=new File(toFolder.getCanonicalPath()+"\\"+fileToSave.getPath());
ClientPanel.fileTransferLabel.setText("File :"+fileToSave.getName());
if(fileToSave.exists())fileToSave.delete();
//System.out.println("idhr ayayyy1");
File p=fileToSave.getParentFile();
if(p!=null)
{
try
{
//System.out.println("parent "+p.getName());
if(p.isDirectory()==false)p.delete();
p.mkdirs();
}catch(Exception e){
//System.out.println(e);
}
//System.out.println("Parent file:"+fileToSave.getParentFile());
}
//System.out.println("idhr aya2");
fileToSave.createNewFile();
//System.out.println("idhr ayayyy3");
fos=new FileOutputStream(fileToSave);
int byteCount;
InputStream is=inputStream;
ByteArrayOutputStream baos=null;
byte[] ack=new byte[1];
OutputStream os=socket.getOutputStream();
//os.write((byte)77);
//os.flush();
byte [] objectLengthInBytes=new byte[8];
byteCount=is.read(objectLengthInBytes);
int objectLength;
objectLength=(objectLengthInBytes[0] & 0xFF) << 56 | (objectLengthInBytes[1] & 0xFF) <<
48 | (objectLengthInBytes[2] & 0xFF) << 40 | (objectLengthInBytes[3] & 0xFF) << 32 | (objectLengthInBytes[4] & 0xFF) << 24 | (objectLengthInBytes[5] & 0xFF) << 16 | (objectLengthInBytes[6] & 0xFF) << 8 | (objectLengthInBytes[7] & 0xFF);
ack[0]=77;
os.write(ack,0,1);
os.flush();
baos=new ByteArrayOutputStream();
byte chunk[]=new byte[1024];
int bytesToRead=objectLength;
int val=0;
while(bytesToRead>0)
{
byteCount=is.read(chunk);
SocketCommunicator.read+=byteCount;
val=(int)(((double)SocketCommunicator.read/(double)SocketCommunicator.size)*100);
ClientPanel.transferProgress.setValue(val);
os.write(ack,0,1);
os.flush();
if(byteCount>0)
{
fos.write(chunk,0,byteCount);
}
bytesToRead-=byteCount;
}
os.write(ack,0,1);
os.flush();
fos.close();
}catch(Exception e)
{
//System.out.println(e);
if(fos!=null)try{
    fos.close();
    }catch(IOException ioe){
        //System.out.println(ioe);
    }
}
}//recv file to
}//class ends