import java.io.*;
import java.util.*;
import java.net.*;
public class FolderObserver extends Thread
{
private LinkedList<FilePojo> targetFolders=new LinkedList<FilePojo>();
private Vector<FilePojo> newTargetFolders=new Vector<FilePojo>();
private LinkedList<Socket> clients;
public static ClientPanel clientPanel;
private volatile boolean running=true;
public volatile boolean fileEventHandlersReady=false;
public LinkedList<FileEventHandler> fileEventHandlers;
public FolderObserver(LinkedList<Socket> clients,LinkedList<File> targetFolders)
{
this.clients=clients;
for(File each:targetFolders)
{
this.targetFolders.add(new FilePojo(each,each));
}
//startup();
//this.fileEventHandler=new FileEventHandler(client,this.targetFolders);
//this.fileEventHandler.start();
}
public void terminate()
{
this.running=false;
for(FileEventHandler fileEventHandler:this.fileEventHandlers)fileEventHandler.terminate();
//this.fileEventHandler.interrupt();
try
{
for(FileEventHandler fileEventHandler:this.fileEventHandlers)fileEventHandler.join();
}catch(InterruptedException ie)
{
//System.out.println(ie);
}
//System.out.println("idhr1");
}
public void setNewTargetFolders(LinkedList<FilePojo> ntf)
{
for(FilePojo each:ntf)this.newTargetFolders.add(each);
}
/*public void startup(Socket client,String serverName)
{
this.clientPanel.showWaitingDialog("Please wait backing up files onto "+serverName);
try
{
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
}*/
public void run()
{
FileEventHandler fhe=null;
this.fileEventHandlers=new LinkedList<>();
int serverNo=0;
for(Socket each:this.clients)
{
serverNo++;
//startup(each,"Server-"+String.valueOf(serverNo));
fhe=new FileEventHandler(each,this.targetFolders,this.clientPanel,serverNo);

fhe.startup();
fhe.start();
this.fileEventHandlers.add(fhe);
}
//this.clientPanel.hideWaitingDialog();
this.fileEventHandlersReady=true;
this.running=true;
LinkedList<File> files=this.getFilesRecursively(this.targetFolders);
LinkedList<File> filesToAdd=null;
FileEvent fe=null;
HashMap<String,Long> lastModifiedDates=new HashMap<>();
for(File eachFile:files)
{
lastModifiedDates.put(eachFile.getPath(),eachFile.lastModified());
}
LinkedList<File> filesToRemove=null;
while(running)
{
try
{
Thread.sleep(500);
}catch(InterruptedException ie){
//System.out.println(ie);
}
filesToRemove=new LinkedList<>();
for(File eachFile:files)
{
if(eachFile.exists()==false)
{
//System.out.println("File deleted :"+eachFile.getPath());
for(FileEventHandler fileEventHandler:this.fileEventHandlers)
{
    fileEventHandler.addEvent(new FileEvent(FileEvent.FileDeleted,eachFile));
}
lastModifiedDates.remove(eachFile.getPath());
filesToRemove.add(eachFile);
}
else if(eachFile.lastModified()>lastModifiedDates.get(eachFile.getPath()))
{
////////////////////////////////////////////////////////////////////////////////////////for folder,just updating modified dates for now,not notifying
if(eachFile.isDirectory()==false)
{
//System.out.println("File modified :"+eachFile.getPath());
for(FileEventHandler fileEventHandler:this.fileEventHandlers)
{
    fileEventHandler.addEvent(new FileEvent(FileEvent.FileModified,eachFile));
}
}
lastModifiedDates.put(eachFile.getPath(),eachFile.lastModified());
}
}//for
for(File each:filesToRemove)
{
files.remove(each);
}//files to remove loop
//Adding new files that may have been created
filesToAdd=this.getNewFilesRecursively(lastModifiedDates);
if(filesToAdd!=null)
{
for(File fileToAdd:filesToAdd)
{
//System.out.println("File created :"+fileToAdd.getPath());
for(FileEventHandler fileEventHandler:this.fileEventHandlers)
{
    fileEventHandler.addEvent(new FileEvent(FileEvent.FileCreated,fileToAdd));
}
lastModifiedDates.put(fileToAdd.getPath(),fileToAdd.lastModified());
files.add(fileToAdd);
}//filesToAdd
}
filesToAdd=null;
}//while
//System.out.println("Folder observer run ended");
}//run

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

public LinkedList<File> getNewFilesRecursively(HashMap<String,Long> existingFiles)
{
LinkedList<File> files=new LinkedList<File>();
Stack<File> folders=new Stack<File>();
File[] temp=null;
for(FilePojo each:this.targetFolders)
{
folders.push(each.clientFile);
}
while(folders.size()!=0)
{
temp=folders.pop().listFiles();
for(File each:temp)
{
if(each.isDirectory())folders.push(each);
if(existingFiles.containsKey(each.getPath())==false)files.add(each);
}
}//while
return files;
}//getNewFilesRecursively
}