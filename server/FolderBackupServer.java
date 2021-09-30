import java.io.*;
import java.util.*;
import java.net.*;
public class FolderBackupServer extends Thread
{
private Socket client;
LinkedList<FilePojo> targetFoldersList;
public FolderBackupServer(Socket client)
{
this.client=client;
FilePojo.serverSide=true;
}
public void startup()
{
try
{
byte[] task=new byte[1];
int byteCount=0;
InputStream is=client.getInputStream();
this.targetFoldersList=(LinkedList<FilePojo>)SocketCommunicator.recvObject(this.client,is);
for(FilePojo eachTargetFolder:targetFoldersList)
{
System.out.println("target folder on server :"+eachTargetFolder.serverFile.getPath());
File targetFolder=eachTargetFolder.serverFile;
if(targetFolder.exists()==false)targetFolder.mkdirs();
}
LinkedList<FilePojo> filesPojo=(LinkedList<FilePojo>)SocketCommunicator.recvObject(this.client,is);
LinkedList<FilePojo> filesToReceive=new LinkedList<>();
for(FilePojo each:filesPojo)
{
System.out.println("Files on client side :"+each.serverFile.getPath());
if(!each.serverFile.exists() && !each.isDirectory)filesToReceive.add(each);
if(each.isDirectory)each.serverFile.mkdirs();
}
SocketCommunicator.sendObject(this.client,filesToReceive);
for(FilePojo each:filesToReceive)System.out.println("File to receive :"+each.serverFile.getPath());
for(FilePojo each:filesToReceive)
{
SocketCommunicator.recvFile(this.client,this.client.getInputStream(),each);
}
}catch(Exception e){
System.out.println(e);
return;
}
}
public void run()
{
try
{

/*while(true)
{
String msg=(String)SocketCommunicator.recvObject(this.client,this.client.getInputStream());
if(msg.equals("start_diskobserver"))break;
if(msg.equals("ping"))
{
SocketCommunicator.sendObject(this.client,"pong");
this.client.close();
return;
}
}*/
startup();


while(true)
{
FileEvent event=(FileEvent)SocketCommunicator.recvObject(this.client,this.client.getInputStream());
System.out.println("Event from client :"+event.event);
handleFileEvent(event);
}
}catch(Exception e){
System.out.println(e);
}
}//run ends
public void handleFileEvent(FileEvent event)
{
FilePojo filePojo=event.filePojo;
if(filePojo!=null)System.out.println("event :"+filePojo.relativePath);
try
{
if(event.event==FileEvent.FileDeleted)
{
System.out.println("File to delete :"+filePojo.serverFile.getPath());
if(!filePojo.serverFile.isDirectory())
{
if(filePojo.serverFile.exists())filePojo.serverFile.delete();
}
else
{
if(filePojo.serverFile.exists())deleteFolder(filePojo.serverFile);
}
}
if(event.event==FileEvent.RestoreFile)
{
System.out.println("File to restore "+event.filePath);

if(event.filePojo.isDirectory==false)
{
FilePojo tmp=new FilePojo();
tmp.serverFile=new File(event.filePath);
SocketCommunicator.sendObject(this.client,new Long(tmp.serverFile.length()));
SocketCommunicator.sendFile(this.client,tmp);
}
else 
{
LinkedList<FilePojo> filesToSend=(LinkedList<FilePojo>)getFilesRecursively(event.filePojo);
long size=0;
for(FilePojo each:filesToSend)size+=each.serverFile.length();
SocketCommunicator.sendObject(this.client,new Long(size));
SocketCommunicator.sendObject(this.client,filesToSend);
try
{
for(FilePojo each:filesToSend)
{
Thread.sleep(100);
System.out.println(each.serverFile.getPath());
SocketCommunicator.sendFile(this.client,each);
}
}catch(Exception e){System.out.println(e);}
}
}
if(event.event==FileEvent.GetServerFolder)
{
FilePojo fp=new FilePojo();
fp.relativePath=System.getProperty("user.dir");
SocketCommunicator.sendObject(this.client,fp);
}
if(event.event==FileEvent.FileModified)
{
SocketCommunicator.recvFile(this.client,this.client.getInputStream(),filePojo);
}
if(event.event==FileEvent.FileCreated)
{
if(!filePojo.isDirectory)SocketCommunicator.recvFile(this.client,this.client.getInputStream(),filePojo);
else filePojo.serverFile.mkdirs();
}
if(event.event==FileEvent.GetTargetFolders)
{
SocketCommunicator.sendObject(this.client,this.targetFoldersList);
}
if(event.event==FileEvent.GetDir)
{
String currentDir=System.getProperty("user.dir");
System.out.println("Get DIR"+event.filePath);
if(currentDir.equals(event.filePath))
{
SocketCommunicator.sendObject(this.client,this.targetFoldersList);
return;
}
File tmp=new File(event.filePath);
File[] dir=tmp.listFiles();
LinkedList<FilePojo> pojoDir=new LinkedList<FilePojo>();
for(File each:dir)pojoDir.add(new FilePojo(each,this.targetFoldersList));
SocketCommunicator.sendObject(this.client,pojoDir);
}
}
catch(Exception e){
System.out.println(e);
}
}////Handle file Event
public LinkedList<FilePojo> getFilesRecursively(FilePojo folderPojo)
{
LinkedList<FilePojo> files=new LinkedList<FilePojo>();
Stack<File> folders=new Stack<File>();
File[] temp=null;
folders.push(folderPojo.serverFile);
while(folders.size()!=0)
{
temp=folders.pop().listFiles();
for(File each:temp)
{
if(each.isDirectory())folders.push(each);
else 
{
FilePojo tmp=new FilePojo(each,this.targetFoldersList);
tmp.relativePath=tmp.relativePath.substring(System.getProperty("user.dir").length()+1);
files.add(tmp);
}
}
}//while
return files;
}//getFilesRecursively
public void deleteFolder(File folder)
{
File[] files = folder.listFiles();
if(files!=null) 
{ 
for(File f: files) {
if(f.isDirectory()) 
{
deleteFolder(f);
} 
else 
{
f.delete();
}
}
}
System.out.println("haooo"+folder.getPath());
folder.delete();
}//deleteFolder
}