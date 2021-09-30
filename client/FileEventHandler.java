import java.net.*;
import java.io.*;
import java.util.*;
public class FileEventHandler extends Thread
{
public static ClientPanel clientPanel;
public static String serverPath;
public Vector<FileEvent> events;
public Socket client;
public LinkedList<FilePojo> targetFolders;
public Vector<FilePojo> filesList=new Vector<FilePojo>();
private volatile boolean running=true;
public volatile boolean dirReady=false;
private int serverNo;
public FileEventHandler(Socket client,LinkedList<FilePojo> targetFolders,ClientPanel clientPanel,int serverNo)
{
this.client=client;
this.clientPanel=clientPanel;
this.targetFolders=targetFolders;
this.events=new Vector<FileEvent>();
this.serverNo=serverNo;
}
public void addEvent(FileEvent fe)
{
FileEvent f=null;
if(fe.event==FileEvent.FileDeleted)
{
for(FileEvent each:events)
{
if(each.event==FileEvent.FileCreated && each.file.getPath().equals(fe.file.getPath()))
{
f=each;
break;
}
}
if(f!=null)events.remove(f);
}
this.events.add(fe);
}
public void addPriorityEvent(FileEvent fe)
{
    /*FileEvent f=null;
    if(fe.event==FileEvent.FileDeleted)
    {
    for(FileEvent each:events)
    {
    if(each.event==FileEvent.FileCreated && each.file.getPath().equals(fe.file.getPath()))
    {
    f=each;
    break;
    }
    }
    if(f!=null)events.remove(f);
    }*/
    this.events.add(0,fe);
}
public void terminate()
{
this.running=false;
}

public void startup()
{
try
{
FileEventHandler.this.clientPanel.showWaitingDialog("Backing up files onto Server-"+String.valueOf(this.serverNo));
SocketCommunicator.sendObject(client,this.targetFolders);
LinkedList<File> files=this.getFilesRecursively(this.targetFolders);
LinkedList<FilePojo> filesPojo=new LinkedList<>();
for(File each:files)filesPojo.add(new FilePojo(each,this.targetFolders));
SocketCommunicator.sendObject(client,filesPojo);
LinkedList<FilePojo> filesToSend=(LinkedList<FilePojo>)SocketCommunicator.recvObject(client,client.getInputStream());
//System.out.println("Files to send at startup****");
//if(filesToSend!=null)for(FilePojo each:filesToSend)System.out.println(each.clientFile.getPath());
//System.out.println("*****************");
SocketCommunicator.size=0;
SocketCommunicator.read=0;
for(FilePojo each:filesToSend)
{
SocketCommunicator.size+=each.clientFile.length();
}
for(FilePojo each:filesToSend)
{
SocketCommunicator.sendFile(client,each);
Thread.sleep(100);
}
}catch(Exception e){
//System.out.println(e);
}
this.clientPanel.hideWaitingDialog();
}

public LinkedList<File> getFilesRecursively(LinkedList<FilePojo> foldersPojo)
{
LinkedList<File> files=new LinkedList<File>();
Stack<File> folders=new Stack<File>();
File[] temp=null;
for(FilePojo each:foldersPojo)
{
folders.push(each.clientFile);
}
while(folders.size()!=0)
{
temp=folders.pop().listFiles();
for(File each:temp)
{
if(each.isDirectory())folders.push(each);
files.add(each);
}
}//while
return files;
}//getFilesRecursively

public void run()
{
//this.startup("Server-"+String.valueOf(this.serverNo));
while(running)
{
FileEvent fe=null;
FilePojo fp=null;
if(this.events.size()!=0)
{
fe=this.events.remove(0);
if(fe==null)
{
//System.out.println("continue");
continue;
}
if(fe.file!=null)fp=new FilePojo(fe.file,this.targetFolders);
else fp=null;
if(fe.filePojo==null)fe.filePojo=fp;

if(fe.event==FileEvent.GetTargetFolders)
{
try
{
Thread.sleep(150);
SocketCommunicator.sendObject(this.client,fe);
LinkedList<FilePojo> ll=(LinkedList<FilePojo>)SocketCommunicator.recvObject(this.client,this.client.getInputStream());
this.filesList.addAll(ll);
//for(FilePojo each:ll)System.out.println(each.serverFile.getName());
}catch(Exception ie){
//System.out.println(ie);
}
}
if(fe.event==FileEvent.GetServerFolder)
{
try
{
Thread.sleep(150);
SocketCommunicator.sendObject(this.client,fe);
FilePojo ll=(FilePojo)SocketCommunicator.recvObject(this.client,this.client.getInputStream());
this.filesList.add(ll);
}catch(Exception ie){
//System.out.println(ie);
}
}
if(fe.event==FileEvent.GetDir)
{
try
{
SocketCommunicator.sendObject(this.client,fe);
Thread.sleep(150);
LinkedList<FilePojo> dir=(LinkedList<FilePojo>)SocketCommunicator.recvObject(this.client,this.client.getInputStream());
this.filesList.addAll(dir);
this.dirReady=true;
}catch(Exception e){
//System.out.println(e);
}
}
if(fe.event==FileEvent.FileCreated)
{
try
{
SocketCommunicator.sendObject(this.client,fe);
Thread.sleep(150);
//System.out.println("Sending file");
SocketCommunicator.sendFile(this.client,fp);
Thread.sleep(150);
}catch(InterruptedException ie){
//System.out.println(ie);
}
}
if(fe.event==FileEvent.RestoreFile)
{
try
{
//System.out.println(fe.filePojo.relativePath);
//System.out.println(fe.filePojo.isDirectory);
this.clientPanel.showWaitingDialog("Please wait while restoring files.");
SocketCommunicator.sendObject(this.client,fe);
SocketCommunicator.size=(Long)SocketCommunicator.recvObject(this.client,this.client.getInputStream());
SocketCommunicator.read=0;
Thread.sleep(150);
if(fe.filePojo.isDirectory==false)
{
//System.out.println("file");
File tmp=new File(fe.filePath);
File fileToSave=new File(tmp.getName());
FilePojo tmppojo=new FilePojo();
tmppojo.clientFile=fileToSave;
SocketCommunicator.recvFileTo(this.client,this.client.getInputStream(),tmppojo);
Thread.sleep(150);
}
else
{
//System.out.println("directory");
//System.out.println("idhr1");
LinkedList<FilePojo> filesToReceive=(LinkedList<FilePojo>)SocketCommunicator.recvObject(this.client,this.client.getInputStream());
//System.out.println("idhr2");
for(FilePojo each:filesToReceive)
{
//System.out.println(fe.filePojo.relativePath);
//System.out.println(each.relativePath);
//File tmp=new File(each.relativePath.substring(fe.filePojo.relativePath.length()-fe.filePojo.serverFile.getName().length()));
File tmp=new File(each.relativePath);
FilePojo tmpPojo=new FilePojo();
//System.out.println("Rel path :"+tmp.getPath());
tmpPojo.clientFile=tmp;
SocketCommunicator.recvFileTo(this.client,this.client.getInputStream(),tmpPojo);
}
}
this.clientPanel.hideWaitingDialog();
}catch(Exception e){
//System.out.println(e);
this.clientPanel.hideWaitingDialog();
}
}
if(fe.event==FileEvent.FileModified)
{
try
{
SocketCommunicator.sendObject(this.client,fe);
Thread.sleep(150);
SocketCommunicator.sendFile(this.client,fp);
Thread.sleep(150);
}catch(InterruptedException ie){
//System.out.println(ie);
}
}
if(fe.event==FileEvent.FileDeleted)
{
try
{
SocketCommunicator.sendObject(this.client,fe);
Thread.sleep(150);
}catch(InterruptedException ie){
//System.out.println(ie);
}
//System.out.println("File event sent");
}
}
}//running
//System.out.println("File event handler run ended,closing connection");
try
{
this.client.close();
}catch(IOException ioe){
//System.out.println(ioe);
}
}

}