import java.io.*;
public class FileEvent implements Serializable
{
public static int FileCreated=1;
public static int FileModified=2;
public static int FileDeleted=3;
public static int GetTargetFolders=4;
public static int GetDir=5;
public static int GetServerFolder=6;
public static int RestoreFile=7;
public int event=0;
public String filePath;
public File file;
public FilePojo filePojo;
public FileEvent(int event)
{
this.event=event;
}
public FileEvent(int event,String filePath)
{
this.event=event;
this.filePath=filePath;
}
public FileEvent(int event,File file)
{
this.event=event;
this.file=file;
}
public FileEvent(int event,File file,FilePojo filePojo)
{
this.event=event;
this.file=file;
this.filePojo=filePojo;
this.filePath=filePojo.relativePath;
}
}