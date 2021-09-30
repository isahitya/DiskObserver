import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
public class DiskObserver
{
public static void main(String gg[])
{
try
{
initFrame();
/*
LinkedList<File> ll=new LinkedList<File>();
ll.add(new File(gg[0]));
ll.add(new File(gg[1]));
FolderObserver fo=new FolderObserver(new Socket("localhost",5555),ll);
fo.start();
*/
}catch(Exception e){
//System.out.println(e);
}
}
public static void initFrame()
{
JFrame frame=new JFrame();
frame.setSize(600,650);
Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
frame.setLocation(((int)d.getWidth()/2)-600/2,((int)d.getHeight()/2)-650/2);
frame.setTitle("DiskObserver");
frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
ClientPanel cp=new ClientPanel(600,650,frame);
frame.add(cp);
cp.diskObserver=frame;

frame.setVisible(true);

}
}