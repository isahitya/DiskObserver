import java.util.*;
import java.io.*;
public class Configuration implements Serializable
{
public LinkedList<File> targetFolders=new LinkedList<>();
public List<String> backupServerIps;
public int port=-1;
}