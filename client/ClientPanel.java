import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.filechooser.*;
import javax.swing.JFileChooser;
import java.io.*;
import java.util.*;
import java.net.*;
public class ClientPanel extends JPanel implements ActionListener,ListSelectionListener,ListCellRenderer
{
private int width;
private int height;
private FolderObserver folderObserver;
private Configuration conf=new Configuration();
private JLabel configurationLabel=new JLabel("Configuration");
private JLabel targetFoldersLabel=new JLabel("Folders to backup :");
private JList targetFoldersList= new JList();
private JScrollPane targetFoldersListScrollPane;
private DefaultListModel<File> targetFoldersModel=new DefaultListModel<>();
private JButton addFolderButton=new JButton("Add Folder");
private JLabel pleaseSelectFolderLabel=new JLabel("Plesae add folder(s) to backup.");
private JButton removeFolderButton=new JButton("Remove");
private JLabel backupServersLabel=new JLabel("Available Servers :");
private JLabel selectedBackupServersLabel=new JLabel("Selected Servers :");
private JList backupServersList=new JList();
private JList selectedBackupServersList=new JList();
private JScrollPane backupServersListScrollPane;
private JScrollPane selectedBackupServersListScrollPane;
private DefaultListModel<String> backupServersModel=new DefaultListModel<>();
private DefaultListModel<String> selectedBackupServersModel=new DefaultListModel<>();
private JButton refreshServersButton=new JButton();
private JButton selectServerButton=new JButton(">");
private JButton removeServerButton=new JButton("<");
private JLabel pleaseSelectServerLabel=new JLabel("Please select a backup server.");
private JLabel fetchingServersLabel=new JLabel("Fetching list of servers...");
private JLabel pleaseWaitLabel=new JLabel("Please wait...");
private JButton cancelButton=new JButton("Cancel");
private JButton startButton=new JButton("Save");
private JLabel foldersOnServerLabel=new JLabel("Restore files from backup");
private JButton backButton=new JButton(new ImageIcon("icons/back_icon.png"));
private JButton homeButton=new JButton();
private JList foldersOnServerList=new JList();
private DefaultListModel<FilePojo> foldersOnServerModel=new DefaultListModel<FilePojo>();
private JScrollPane foldersOnServerListScrollPane;
private JLabel pleaseSelectFileToRestoreLabel=new JLabel("Please select a file/folder to restore.");
private JButton restoreButton=new JButton("Restore file/folder");
private JButton reconfigureButton=new JButton();
private JLabel restoreFilesToDestinationLabel=new JLabel("Restore files to destination :");
private JTextField restoreFilesAtField=new JTextField();
private JButton restoreFilesToButton=new JButton();
private JDialog waitingDialog=null; 
private boolean wasBackEnabled=false;
private boolean wasHomeEnabled=false;
public JFrame diskObserver;
public static JProgressBar transferProgress=new JProgressBar(SwingConstants.HORIZONTAL);
public static JLabel fileTransferLabel=new JLabel();
private int port=5555;
private String serverPath=null;
private String currentDirectory="";
private Thread getTargetFoldersList;
private JButton restoreAllButton=new JButton("Restore Everything");
public ClientPanel(int width,int height,JFrame diskObserver)
{
this.diskObserver=diskObserver;
FileEventHandler.clientPanel=this;
FolderObserver.clientPanel=this;
this.setBounds(0,0,width,height);
this.width=width;
this.height=height;
this.setLayout(null);
this.setVisible(true);
File confFile=new File("configuration.dat");
if(confFile.exists()==false)showConfigurationView();
else showApplicationView(true);
}
public void showConfigurationView()
{
File confFile=new File("configuration.dat");
if(confFile.exists()==false)
{
//System.out.println("No configuration file");
}
else
{
try
{
FileInputStream fis=new FileInputStream(confFile);
ObjectInputStream ois=new ObjectInputStream(fis);
this.conf=(Configuration)ois.readObject();
LinkedList<FilePojo> tflpojo=new LinkedList<>();
LinkedList<File> tfl=this.conf.targetFolders;
FilePojo tf=null;
java.util.List<String> sbsm=this.conf.backupServerIps;
this.targetFoldersModel.clear();
this.selectedBackupServersModel.clear();
for(File each:tfl)
{
this.targetFoldersModel.addElement(each);
}
for(String each:sbsm)
{
this.selectedBackupServersModel.addElement(each);
}
this.add(cancelButton);
}catch(Exception e){
//System.out.println(e);
}
}
this.configurationLabel.setFont(new Font("Arial",Font.BOLD,24));
this.targetFoldersLabel.setFont(new Font("Arial",Font.PLAIN,18));
this.pleaseSelectFolderLabel.setFont(new Font("Arial",Font.BOLD,14));
this.pleaseSelectServerLabel.setFont(new Font("Arial",Font.BOLD,14));
this.backupServersLabel.setFont(new Font("Arial",Font.PLAIN,18));
this.selectedBackupServersLabel.setFont(new Font("Arial",Font.PLAIN,18));
this.fetchingServersLabel.setFont(new Font("Arial",Font.BOLD,14));
this.pleaseSelectFolderLabel.setVisible(false);
this.pleaseSelectServerLabel.setVisible(false);
this.fetchingServersLabel.setVisible(false);
this.targetFoldersList.setModel(this.targetFoldersModel);
this.targetFoldersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
this.targetFoldersList.setFont(new Font("Arial",Font.PLAIN,16));
if(this.targetFoldersList.getListSelectionListeners().length==0)this.targetFoldersList.addListSelectionListener(this);
if(this.backupServersList.getListSelectionListeners().length==0)this.backupServersList.addListSelectionListener(this);
if(this.selectedBackupServersList.getListSelectionListeners().length==0)this.selectedBackupServersList.addListSelectionListener(this);
if(this.selectServerButton.getActionListeners().length==0)this.selectServerButton.addActionListener(this);
if(this.removeServerButton.getActionListeners().length==0)this.removeServerButton.addActionListener(this);
this.targetFoldersListScrollPane=new JScrollPane(targetFoldersList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
this.backupServersList.setModel(this.backupServersModel);
this.selectedBackupServersList.setModel(this.selectedBackupServersModel);
this.selectedBackupServersList.setModel(this.selectedBackupServersModel);
this.backupServersList.setFont(new Font("Arial",Font.PLAIN,16));
this.selectedBackupServersList.setFont(new Font("Arial",Font.PLAIN,16));
//this.backupServersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
this.backupServersListScrollPane=new JScrollPane(backupServersList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
this.selectedBackupServersListScrollPane=new JScrollPane(selectedBackupServersList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
if(this.addFolderButton.getActionListeners().length==0)this.addFolderButton.addActionListener(this);
if(this.removeFolderButton.getActionListeners().length==0)this.removeFolderButton.addActionListener(this);
this.removeFolderButton.setEnabled(false);
ImageIcon ri=new ImageIcon("icons/refresh_icon.png");
Image scaledri=ri.getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH);
this.refreshServersButton.setIcon(new ImageIcon(scaledri));
if(this.refreshServersButton.getActionListeners().length==0)this.refreshServersButton.addActionListener(this);
if(this.startButton.getActionListeners().length==0)this.startButton.addActionListener(this);
if(this.cancelButton.getActionListeners().length==0)this.cancelButton.addActionListener(this);
this.configurationLabel.setBounds(30,30,200,40);
this.targetFoldersLabel.setBounds(30,30+40+5,200,30);
this.targetFoldersListScrollPane.setBounds(30,30+40+5+30+2,this.width-30-40,140);
this.addFolderButton.setBounds(30,30+40+5+30+2+140+10,100,30);
this.pleaseSelectFolderLabel.setBounds(30+100+5,30+40+5+30+2+140+7,250,40);
this.removeFolderButton.setBounds(30+this.width-30-40-100,30+40+5+30+2+140+10,100,30);
this.backupServersLabel.setBounds(30,30+40+5+30+2+140+7+40+35,200,30);
this.selectedBackupServersLabel.setBounds(30+(this.width/2),30+40+5+30+2+140+7+40+35,200,30);
this.backupServersListScrollPane.setBounds(30,30+40+5+30+2+140+7+40+35+30+5,(this.width/2)-30-40,140);
this.selectedBackupServersListScrollPane.setBounds(30+(this.width/2),30+40+5+30+2+140+7+40+35+30+5,(this.width/2)-30-40,140);
this.refreshServersButton.setBounds((this.width/2)-30-40,30+40+5+30+2+140+7+40+35,30,30);
this.selectServerButton.setBounds((this.width/2)-30-40+40,30+40+5+30+2+140+7+40+35+50,50,30);
this.removeServerButton.setBounds((this.width/2)-30-40+40,30+40+5+30+2+140+7+40+35+50+30+10,50,30);
this.pleaseSelectServerLabel.setBounds(30,30+40+5+30+2+140+7+40+35+30+5+140+5+40+5,250,40);
this.fetchingServersLabel.setBounds(30,30+40+5+30+2+140+7+40+35+30+5+140,200,40);
this.pleaseWaitLabel.setBounds(this.width-30-120,30+40+5+30+2+140+7+40+35+30+5+140+5+40-30,90,30);
this.cancelButton.setBounds(this.width-30-100-90-5,30+40+5+30+2+140+7+40+35+30+5+140+5+40,90,30);
this.startButton.setBounds(this.width-30-100,30+40+5+30+2+140+7+40+35+30+5+140+5+40,90,30);
this.add(selectServerButton);
this.add(removeServerButton);
this.add(configurationLabel);
this.add(targetFoldersLabel);
this.add(targetFoldersListScrollPane);
this.add(addFolderButton);
this.add(pleaseSelectFolderLabel);
this.add(removeFolderButton);
this.add(backupServersLabel);
this.add(selectedBackupServersLabel);
this.add(backupServersListScrollPane);
this.add(selectedBackupServersListScrollPane);
this.add(refreshServersButton);
this.add(fetchingServersLabel);
this.add(pleaseSelectServerLabel);
this.add(startButton);
ServerListGenerator slg=new ServerListGenerator();
slg.start();

}
/*public void setServerListToSelectedServer()
{
Object[] arr=null;
arr=(Object[])this.backupServersModel.toArray();
if(arr.length!=0)
{
for(Object each:arr)
{
//System.out.println((String)each);
if(this.conf.backupServerIp.equals((String)each))
{
this.backupServersList.setSelectedValue((String)each,true);
break;
}
}
}

}*/
public void hideConfigurationView()
{
this.removeAll();
this.revalidate();
this.repaint();
}
public void hideApplicationView()
{
this.foldersOnServerModel.clear();
this.removeAll();
this.revalidate();
this.repaint();
/*try
{
if(this.folderObserver!=null)
{
this.folderObserver.terminate();
this.folderObserver.join();
}
}catch(InterruptedException ie)
{
//System.out.println(ie);
}*/
}
public void disableApplicationView()
{
this.wasBackEnabled=this.backButton.isEnabled();
this.wasHomeEnabled=this.homeButton.isEnabled();
this.backButton.setEnabled(false);
this.homeButton.setEnabled(false);
this.restoreFilesToButton.setEnabled(false);
this.restoreButton.setEnabled(false);
this.foldersOnServerList.setEnabled(false);
this.reconfigureButton.setEnabled(false);
}
public void enableApplicationView()
{
this.backButton.setEnabled(this.wasBackEnabled);
this.homeButton.setEnabled(this.wasHomeEnabled);
this.reconfigureButton.setEnabled(true);
this.foldersOnServerList.setEnabled(true);
this.restoreButton.setEnabled(true);
this.restoreFilesToButton.setEnabled(true);
}
public void showWaitingDialog(String waitMsg)
{
disableApplicationView();
transferProgress.setValue(0);
this.waitingDialog=new JDialog(diskObserver,"Please wait");
this.waitingDialog.setAlwaysOnTop(true);
JLabel msg=new JLabel(""+waitMsg);
this.fileTransferLabel=new JLabel();
msg.setFont(new Font("Arial",Font.PLAIN,14));
fileTransferLabel.setFont(new Font("Arial",Font.PLAIN,13));
msg.setBounds(10,10,270,30);
fileTransferLabel.setBounds(10,10+35,350,30);
transferProgress.setBounds(10,10+50+10,350,30);
waitingDialog.setLayout(null);
waitingDialog.add(msg);
waitingDialog.add(fileTransferLabel);
waitingDialog.add(transferProgress);
this.waitingDialog.setSize(400,150);
//this.waitingDialog.pack();
this.waitingDialog.setLocationRelativeTo(this);
//this.waitingDialog.setUndecorated(true);
waitingDialog.setVisible(true);
waitingDialog.repaint();
}
public void hideWaitingDialog()
{
enableApplicationView();
if(this.waitingDialog==null)return;
this.waitingDialog.setVisible(false);
}
public void initApplication()
{
try
{
    try
    {
    if(this.folderObserver!=null)
    {
    this.folderObserver.terminate();
    this.folderObserver.join();
    }
    }catch(InterruptedException ie)
    {
    //System.out.println(ie);
    }
File confFile=new File("configuration.dat");
if(confFile.exists()==false)
{
//System.out.println("No configuration file");
return;
}
FileInputStream fis=new FileInputStream(confFile);
ObjectInputStream ois=new ObjectInputStream(fis);
this.conf=(Configuration)ois.readObject();
if(this.conf==null)
{
//System.out.println("conf first");
return;
}
Socket sock=null;
boolean connToOneServer=false;
LinkedList<Socket> ll=new LinkedList<>();
for(String backupServerIp:conf.backupServerIps)
{
try
{ 
sock=new Socket();
sock.connect(new InetSocketAddress(backupServerIp,conf.port),7000);
connToOneServer=true;
ll.add(sock);
}catch(SocketTimeoutException ste)
{
//System.out.println("Could not connect to backup server ");
//System.out.println(ste);
JOptionPane.showMessageDialog(this,"Could not connect to server "+backupServerIp+" ,please make sure the server is running.");
}
}
if(connToOneServer==false)System.exit(0);
//this.showWaitingDialog("Please wait while backing up files.");
this.folderObserver=new FolderObserver(ll,conf.targetFolders);
this.folderObserver.start();

}catch(Exception e){
//System.out.println(e);
//System.out.println("Could not init application,closing");
//return;
}
this.getTargetFoldersList=new Thread(){
public void run() 
{
ClientPanel.this.foldersOnServerListGenerator();
}//run
};
this.getTargetFoldersList.start();
/*try
{
this.getTargetFoldersList.join();
}catch(InterruptedException ie)
{
//System.out.println(ie);
}*/
}
public void foldersOnServerListGenerator()
{
try
{
if(ClientPanel.this.folderObserver==null)
{
//System.out.println("Could not start folder observer ,please start the server");
return;
}
while(ClientPanel.this.folderObserver.fileEventHandlersReady==false){Thread.sleep(100);}
ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.clear();
ClientPanel.this.folderObserver.fileEventHandlers.get(0).addEvent(new FileEvent(FileEvent.GetServerFolder));
while(ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.isEmpty())
{
Thread.sleep(100);
//System.out.println("waiting");
}
ClientPanel.this.serverPath=((FilePojo)ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.lastElement()).relativePath;
//System.out.println("Server Path"+ClientPanel.this.serverPath);
FileEventHandler.serverPath=serverPath;
ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.clear();
ClientPanel.this.folderObserver.fileEventHandlers.get(0).addEvent(new FileEvent(FileEvent.GetTargetFolders));
while(ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.isEmpty())
{
Thread.sleep(100);
//System.out.println("waiting");
}
ClientPanel.this.foldersOnServerModel.clear();
for(FilePojo each:ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList)
{
ClientPanel.this.foldersOnServerModel.addElement(each);
//System.out.println("Target folder on server : "+each.serverFile.getPath());
}
ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.clear();
}catch(InterruptedException ie){
//System.out.println(ie);
}
}
public void showApplicationView(boolean initApp)
{
if(initApp)initApplication();
else foldersOnServerListGenerator();
this.foldersOnServerList.setCellRenderer(this);
this.foldersOnServerList.setFixedCellHeight(40);
this.foldersOnServerList.setFont(new Font("Arial",Font.PLAIN,22));
this.foldersOnServerList.setModel(this.foldersOnServerModel);
this.foldersOnServerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//System.out.println(" Mouse listeners :"+this.foldersOnServerList.getMouseListeners().length);
if(this.foldersOnServerList.getMouseListeners().length==2)
{
//System.out.println("Mouse listener was not added");
this.foldersOnServerList.addMouseListener(new FolderListMouseListener());
}
this.foldersOnServerLabel.setFont(new Font("Arial",Font.BOLD,24));
this.restoreFilesToDestinationLabel.setFont(new Font("Arial",Font.PLAIN,18));
this.foldersOnServerListScrollPane=new JScrollPane(foldersOnServerList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
if(this.backButton.getActionListeners().length==0)this.backButton.addActionListener(this);
this.backButton.setEnabled(false);
this.homeButton.setEnabled(false);
this.selectServerButton.setEnabled(false);
this.removeServerButton.setEnabled(false);
ImageIcon bi=new ImageIcon("icons/back_icon.png");
Image scaled=bi.getImage().getScaledInstance(40,40,java.awt.Image.SCALE_SMOOTH);
this.backButton.setIcon(new ImageIcon(scaled));
ImageIcon hi=new ImageIcon("icons/home_icon.png");
Image scaledhi=hi.getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH);
this.homeButton.setIcon(new ImageIcon(scaledhi));
ImageIcon ci=new ImageIcon("icons/configure_icon.png");
Image scaledci=ci.getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH);
this.reconfigureButton.setIcon(new ImageIcon(scaledci));
ImageIcon ri=new ImageIcon("icons/restore_to_button.png");
Image scaledri=ri.getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH);
this.restoreFilesToButton.setIcon(new ImageIcon(scaledri));
if(this.restoreFilesToButton.getActionListeners().length==0)this.restoreFilesToButton.addActionListener(this);
this.restoreFilesAtField.setText(System.getProperty("user.dir"));
if(this.foldersOnServerList.getListSelectionListeners().length==0)this.foldersOnServerList.addListSelectionListener(this);
if(this.homeButton.getActionListeners().length==0)this.homeButton.addActionListener(this);
if(this.restoreButton.getActionListeners().length==0)this.restoreButton.addActionListener(this);
if(this.restoreAllButton.getActionListeners().length==0)this.restoreAllButton.addActionListener(this);
if(this.reconfigureButton.getActionListeners().length==0)this.reconfigureButton.addActionListener(this);
this.restoreFilesAtField.setEditable(false);
this.pleaseSelectFileToRestoreLabel.setVisible(false);
this.foldersOnServerLabel.setBounds(30,30,300,30);
this.backButton.setBounds(30,30+30+15,70,30);
this.homeButton.setBounds(30+70+5,30+30+15,70,30);
this.reconfigureButton.setBounds(30+this.width-30-40-85+15,30+30+15,70,30);
this.foldersOnServerListScrollPane.setBounds(30,30+30+15+30+4,this.width-30-40,350);
this.pleaseSelectFileToRestoreLabel.setBounds(this.width-40-220,30+30+15+30+350,250,30);
this.restoreFilesToDestinationLabel.setBounds(30,30+30+15+30+4+350+20,300,30);
this.restoreFilesAtField.setBounds(30,30+30+15+30+4+350+10+40,this.width-30-40-70,30);
this.restoreFilesToButton.setBounds(30+this.width-30-40-70+10,30+30+15+30+4+350+10+40,60,30);
this.restoreButton.setBounds(30+this.width-30-40-150,30+30+15+30+4+350+4+100,150,30);
this.restoreAllButton.setBounds(30+this.width-30-40-150-150-20,30+30+15+30+4+350+4+100,150,30);
this.add(foldersOnServerLabel);
this.add(foldersOnServerListScrollPane);
this.add(backButton);
this.add(homeButton);
this.add(pleaseSelectFileToRestoreLabel);
this.add(this.restoreFilesToDestinationLabel);
this.add(restoreFilesAtField);
this.add(restoreFilesToButton);
this.add(restoreButton);
this.add(restoreAllButton);
this.add(reconfigureButton);
}
public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus)
{
FilePojo filePojo=(FilePojo)value;
JPanel component=new JPanel();
component.setBounds(0,0,200,200);
component.setBorder(BorderFactory.createLineBorder(Color.black));
if(isSelected)component.setBackground(new Color(153,204,255));
component.setLayout(null);
JLabel image=new JLabel(filePojo.icon);
image.setBounds(0,0,40,40);
JLabel text=new JLabel(filePojo.serverFile.getName());
text.setFont(new Font("Arial",Font.PLAIN,16));
text.setBounds(40,0,160,40);
component.add(image);
component.add(text);
return component;
}
public class FolderListMouseListener extends MouseAdapter
{
public void mouseClicked(MouseEvent me)
{
if(me.getClickCount()==2)
{
//System.out.println("double click");
FilePojo selectedElement=(FilePojo)ClientPanel.this.foldersOnServerList.getSelectedValue();
if( selectedElement==null || !selectedElement.isDirectory)
{
//System.out.println("wrong");
return;
}
ClientPanel.this.changeDirectory(selectedElement);
return;
}
if(me.getClickCount()==1)
{
//System.out.println("single click");
}
}
}
public void changeDirectory(FilePojo filePojo)
{
boolean whe=ClientPanel.this.homeButton.isEnabled();
boolean wbe=ClientPanel.this.backButton.isEnabled();
File tmp=new File(this.serverPath);
if(filePojo.relativePath.equals(tmp.getParent()))
{
//System.out.println("Can't go further back from here.");
return;
}
if(filePojo.relativePath.equals(this.serverPath))
{
this.homeButton.setEnabled(false);
}else this.homeButton.setEnabled(true);
if(!filePojo.relativePath.equals(this.serverPath))this.backButton.setEnabled(true);
else this.backButton.setEnabled(false);
Thread changeDir=new Thread(){
public void run(){
ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.clear();
//System.out.println("GET DIR"+filePojo.relativePath);
FileEvent fe=new FileEvent(FileEvent.GetDir,filePojo.relativePath);
ClientPanel.this.folderObserver.fileEventHandlers.get(0).addPriorityEvent(fe);
try
{
ClientPanel.this.folderObserver.fileEventHandlers.get(0).dirReady=false;
while(ClientPanel.this.folderObserver.fileEventHandlers.get(0).dirReady==false)
{
Thread.sleep(100);
}
if(ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.isEmpty())
{
    ClientPanel.this.showFolderEmptyMsg();
    ClientPanel.this.homeButton.setEnabled(whe);
    ClientPanel.this.backButton.setEnabled(wbe);
    return;
}

/*while(ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.isEmpty())
{
Thread.sleep(100);
}*/
}catch(InterruptedException ie){
//System.out.println(ie);
}
ClientPanel.this.foldersOnServerModel.clear();
for(FilePojo each:ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList)ClientPanel.this.foldersOnServerModel.addElement(each);
ClientPanel.this.currentDirectory=filePojo.relativePath;
//System.out.println("after chng dir rel path");
//System.out.println(ClientPanel.this.currentDirectory);
}};
changeDir.start();
try
{
changeDir.join();
}catch(InterruptedException ie)
{
//System.out.println(ie);
}
}
public void showFolderEmptyMsg()
{
    //JOptionPane.showMessageDialog(this,"Folder is empty");
}
public void actionPerformed(ActionEvent ae)
{
if(ae.getSource()==this.addFolderButton)
{
JFileChooser chooser = new JFileChooser();
chooser.setDialogTitle("Select folder to backup ");
chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
chooser.setFileFilter(new javax.swing.filechooser.FileFilter(){
public boolean accept(File f){return f.isDirectory();}
public String getDescription(){return "Folders";}
});
int returnVal = chooser.showOpenDialog(this);

if(returnVal == JFileChooser.APPROVE_OPTION && this.targetFoldersModel.contains(chooser.getSelectedFile())==false) 
{
File f=chooser.getSelectedFile();
if(f.exists()==false && f.isDirectory()==false)
{
JOptionPane.showMessageDialog(this, "No such folder exists.");
return;
}
Object[] tarFolders=this.targetFoldersModel.toArray();
for(Object each:tarFolders)
{
    if(((File)each).getPath().startsWith(f.getPath()))
    {
        JOptionPane.showMessageDialog(this,"Cannot add this directory because a directory inside it is already under watch :\n"+((File)each).getPath()+"\nRemove this directory first.");
        return;
    }
    if(f.getPath().startsWith(((File)each).getPath()))
    {
        JOptionPane.showMessageDialog(this,"Directory is already under watch due to parent directory being under watch :\n"+((File)each).getPath());
        return;
    }
}
//System.out.println("Folder to backup : " +chooser.getSelectedFile().getPath());
this.targetFoldersModel.addElement(chooser.getSelectedFile());
this.pleaseSelectFolderLabel.setVisible(false);
}

}//getsource -- add folder
if(ae.getSource()==this.selectServerButton)
{
    //System.out.println("idhr");
    String ip=(String)this.backupServersList.getSelectedValue();
    if(ip!=null)
    {
        this.selectedBackupServersModel.addElement(ip);
        this.backupServersModel.removeElement(ip);
    }
    //if(this.backupServersModel.isEmpty())
    this.selectServerButton.setEnabled(false);
    
}
if(ae.getSource()==this.restoreAllButton)
{
String desFolder=this.restoreFilesAtField.getText();
if(desFolder.trim().length()==0)
{
//System.out.println("Please select a destination folder to restore files at");
return;
}
File desFolderFile=new File(desFolder);
if(desFolderFile.exists()==false || desFolderFile.isDirectory()==false)
{
//System.out.println("Please select a valid directory as destination");
return;
}
//System.out.println("Destination folder set to "+desFolder);
SocketCommunicator.desFolder=desFolderFile;
try
{
System.setProperty("user.dir",desFolderFile.getCanonicalPath());
}catch(IOException ioe)
{
//System.out.println(ioe);
//System.out.println("destination folder problem ");
return;
}
try
{
ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.clear();
ClientPanel.this.folderObserver.fileEventHandlers.get(0).addEvent(new FileEvent(FileEvent.GetTargetFolders));
while(ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.isEmpty())
{
Thread.sleep(100);
//System.out.println("waiting");
}
for(FilePojo each:ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList)
{
    FileEvent fe=new FileEvent(FileEvent.RestoreFile,each.serverFile,each);
    fe.filePojo.isDirectory=each.isDirectory;
    this.folderObserver.fileEventHandlers.get(0).addEvent(fe);
}
ClientPanel.this.folderObserver.fileEventHandlers.get(0).filesList.clear();
}catch(InterruptedException ie)
{
//System.out.println(ie);
}
}//restoreAllButton
if(ae.getSource()==this.restoreFilesToButton)
{

JFileChooser chooser=new JFileChooser();
chooser.setDialogTitle("Select destination folder ");
chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
chooser.setFileFilter(new javax.swing.filechooser.FileFilter(){
public boolean accept(File f){return f.isDirectory();}
public String getDescription(){return "Folders";}
});
int returnVal = chooser.showOpenDialog(this);
if(returnVal==JFileChooser.APPROVE_OPTION)
{
try
{
File f=chooser.getSelectedFile();
if(f.exists()==false && f.isDirectory()==false)
{
JOptionPane.showMessageDialog(this, "No such folder exists.");
return;
}
this.restoreFilesAtField.setText(chooser.getSelectedFile().getCanonicalPath());
}catch(IOException ioe)
{
//System.out.println(ioe);
}
}

}
if(ae.getSource()==this.removeFolderButton)
{
this.targetFoldersModel.removeElement(this.targetFoldersList.getSelectedValue());
}//getsource --remove folder
if(ae.getSource()==this.cancelButton)
{
hideConfigurationView();
showApplicationView(false);
}
if(ae.getSource()==this.refreshServersButton)
{
this.backupServersModel.clear();
(new ServerListGenerator()).start();
}
if(ae.getSource()==this.homeButton)
{
FilePojo fp=new FilePojo();
fp.relativePath=this.serverPath;
this.changeDirectory(fp);
}
if(ae.getSource()==this.removeServerButton)
{
String ip=(String)this.selectedBackupServersList.getSelectedValue();
if(ip!=null)
{
    this.selectedBackupServersModel.removeElement(ip);
    this.backupServersModel.addElement(ip);
    this.removeServerButton.setEnabled(false);
}
}
if(ae.getSource()==this.backButton)
{
FilePojo fp=new FilePojo();
//System.out.println(this.currentDirectory);
File parent=(new File(this.currentDirectory)).getParentFile();
fp.serverFile=parent;
//System.out.println(parent);
if(parent!=null)
{
fp.relativePath=parent.getPath();
this.changeDirectory(fp);
}
else
{
fp.relativePath=this.serverPath;
this.changeDirectory(fp);
this.backButton.setEnabled(false);
}
}
if(ae.getSource()==this.reconfigureButton)
{
hideApplicationView();
showConfigurationView();
}
if(ae.getSource()==this.restoreButton)
{
String desFolder=this.restoreFilesAtField.getText();
if(desFolder.trim().length()==0)
{
//System.out.println("Please select a destination folder to restore files at");
return;
}
File desFolderFile=new File(desFolder);
if(desFolderFile.exists()==false || desFolderFile.isDirectory()==false)
{
//System.out.println("Please select a valid directory as destination");
return;
}
//System.out.println("Destination folder set to "+desFolder);
SocketCommunicator.desFolder=desFolderFile;
try
{
System.setProperty("user.dir",desFolderFile.getCanonicalPath());
}catch(IOException ioe)
{
//System.out.println(ioe);
//System.out.println("destination folder problem ");
return;
}
if(this.foldersOnServerList.getSelectedValue()==null)
{
//System.out.println("Please select a file/folder to restore");
this.pleaseSelectFileToRestoreLabel.setVisible(true);
return;
}

FilePojo filePojoToRestore=(FilePojo)this.foldersOnServerList.getSelectedValue();
//System.out.println("restore"+filePojoToRestore.relativePath);
FileEvent fe=new FileEvent(FileEvent.RestoreFile,filePojoToRestore.serverFile,filePojoToRestore);
fe.filePojo.isDirectory=filePojoToRestore.isDirectory;
//System.out.println(filePojoToRestore.isDirectory);
//System.out.println(fe.filePojo.isDirectory);
this.folderObserver.fileEventHandlers.get(0).addEvent(fe);
//System.out.println("restore clicked");
}
if(ae.getSource()==this.startButton)
{
//this.add(pleaseWaitLabel);

this.pleaseSelectFolderLabel.setVisible(false);
this.pleaseSelectServerLabel.setVisible(false);
boolean everythingGood=true;
if(this.targetFoldersModel.isEmpty())
{
this.pleaseSelectFolderLabel.setVisible(true);
everythingGood=false;
}
if(this.selectedBackupServersModel.isEmpty())
{
this.pleaseSelectServerLabel.setVisible(true);
everythingGood=false;
}
if(everythingGood)
{

    if(this.folderObserver!=null)
    {
    this.folderObserver.terminate();
    try
    {
    this.folderObserver.join();
    }catch(InterruptedException ie)
    {
    //System.out.println(ie);
    }
    }

this.conf=new Configuration();
for(int x=0;x<this.targetFoldersModel.getSize();x++)conf.targetFolders.add(this.targetFoldersModel.get(x));
java.util.List<String> temp=new java.util.LinkedList<>();
Object[] iparr=this.selectedBackupServersModel.toArray();
for(Object each:iparr)temp.add((String)each);
conf.backupServerIps=temp;
conf.port=this.port;
try
{
ByteArrayOutputStream baos=new ByteArrayOutputStream();
ObjectOutputStream oos=new ObjectOutputStream(baos);
oos.writeObject(conf);
File confFile=new File("configuration.dat");
if(confFile.exists())confFile.delete();
FileOutputStream fos=new FileOutputStream(confFile);
fos.write(baos.toByteArray());
fos.close();
}catch(IOException ioe)
{
//System.out.println("Couldn't save settings");
return;
}
hideConfigurationView();
showApplicationView(true);
}
}//start button
}//action performed
public void valueChanged(ListSelectionEvent lse)
{
if(lse.getSource()==this.targetFoldersList)
{
if(lse.getValueIsAdjusting())return;
if(this.targetFoldersModel.isEmpty()) this.removeFolderButton.setEnabled(false);
else this.removeFolderButton.setEnabled(true);
}
if(lse.getSource()==this.foldersOnServerList)
{
this.pleaseSelectFileToRestoreLabel.setVisible(false);
}
if(lse.getSource()==this.backupServersList)
{
if(this.backupServersList.getSelectedValue()!=null)this.selectServerButton.setEnabled(true);
}
if(lse.getSource()==this.selectedBackupServersList)
{
if(this.selectedBackupServersList.getSelectedValue()!=null)this.removeServerButton.setEnabled(true);
}
}

public class ServerListGenerator extends Thread
{
private Thread thisThread;
private final int timeToRun = 7000;
private byte[] buf=new byte[256];
public void run()
{
try
{
ClientPanel.this.backupServersModel.clear();
ClientPanel.this.refreshServersButton.setEnabled(false);
ClientPanel.this.fetchingServersLabel.setText("Fetching list of servers...");
ClientPanel.this.fetchingServersLabel.setVisible(true);
thisThread = Thread.currentThread();
new Thread(new Runnable() {
public void run() {
try
{
sleep(timeToRun);
thisThread.interrupt();
}catch(InterruptedException ie){
//System.out.println(ie);
}
}}).start();
int x=0;
DatagramSocket socket=new DatagramSocket();
broadcast(socket,"ping",InetAddress.getByName("255.255.255.255"));
socket.setSoTimeout(1000);
while(!thisThread.isInterrupted())
{
try
{
DatagramPacket recvPacket=new DatagramPacket(buf,buf.length);
socket.receive(recvPacket);
String received = new String(recvPacket.getData(), 0, recvPacket.getLength());
if(received.equals("pong"))
{
    String ip=recvPacket.getAddress().getHostAddress();
    if(ClientPanel.this.selectedBackupServersModel.contains(ip)==false)ClientPanel.this.backupServersModel.addElement(ip);
}
//if(ClientPanel.this.conf.backupServerIp.equals(recvPacket.getAddress().getHostAddress()))setServerListToSelectedServer();
}catch(SocketTimeoutException ste){}
}
socket.close();
if(ClientPanel.this.backupServersModel.isEmpty())ClientPanel.this.fetchingServersLabel.setText("No backup servers found.");
if(!ClientPanel.this.backupServersModel.isEmpty())ClientPanel.this.fetchingServersLabel.setVisible(false);

ClientPanel.this.refreshServersButton.setEnabled(true);
}catch(Exception e)
{
//System.out.println("Couldn't get list of servers");
ClientPanel.this.refreshServersButton.setEnabled(true);
if(!ClientPanel.this.backupServersModel.isEmpty())ClientPanel.this.fetchingServersLabel.setVisible(false);
}
}//run
public void broadcast(DatagramSocket socket,String broadcastMessage, InetAddress address) throws IOException {
socket.setBroadcast(true);
byte[] buffer = broadcastMessage.getBytes();
DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, ClientPanel.this.port);
socket.send(packet);
}
}//inner class
}//class ends