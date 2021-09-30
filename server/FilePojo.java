import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
public class FilePojo implements Serializable
{
public static boolean serverSide=false;
public File serverFile;
public File clientFile;
public String relativePath;
public Boolean isDirectory;
public Icon icon;
public FilePojo()
{
}
public FilePojo(File file,String relativePath)
{
this.relativePath=relativePath;
this.serverFile=new File(relativePath);
this.clientFile=file;
this.isDirectory=file.isDirectory();
if(serverSide)this.icon=FileSystemView.getFileSystemView().getSystemIcon(this.serverFile);
else this.icon=FileSystemView.getFileSystemView().getSystemIcon(this.clientFile);
}
public FilePojo(File file,File targetFolder)
{
try
{
this.relativePath=file.getCanonicalPath();
this.relativePath=this.relativePath.substring(targetFolder.getCanonicalPath().length()-targetFolder.getName().length());
}catch(IOException ioe){System.out.println(ioe);}
this.serverFile=new File(relativePath);
this.clientFile=file;
this.isDirectory=file.isDirectory();
if(serverSide)this.icon=FileSystemView.getFileSystemView().getSystemIcon(this.serverFile);
else this.icon=FileSystemView.getFileSystemView().getSystemIcon(this.clientFile);

}
public FilePojo(File file,LinkedList<FilePojo> targetFolders)
{
try
{
this.relativePath=file.getCanonicalPath();
for(FilePojo each:targetFolders)
{
if(file.getCanonicalPath().startsWith(each.clientFile.getCanonicalPath()))
{
this.relativePath=this.relativePath.substring(each.clientFile.getCanonicalPath().length()-each.clientFile.getName().length());
break;
}
}
}catch(IOException ioe){System.out.println(ioe);}
this.serverFile=new File(relativePath);
this.clientFile=file;
this.isDirectory=file.isDirectory();
if(serverSide)this.icon=FileSystemView.getFileSystemView().getSystemIcon(this.serverFile);
else this.icon=FileSystemView.getFileSystemView().getSystemIcon(this.clientFile);
}
}