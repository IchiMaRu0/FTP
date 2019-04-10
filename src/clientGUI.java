import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class clientGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel panelTop;
    private JTextArea txtAddr;
    private JTextArea txtUsername;
    private JButton btnConnect;
    private JLabel lblAddr;
    private JPanel panelCenter;
    private JPanel panelCenterTop;
    private JButton btnBrowse;
    private JLabel lblFilePath;
    private JPasswordField passwordField;
    private JPanel panelCenterBottom;
    private JButton btnDownload;
    private JButton btnUpload;
    private JButton btnChoose;
    private JLabel lblDestDir;
    private JPanel panelBottom;
    private JProgressBar progBar;
    private JButton btnPause;
    private JButton btnCancel;
    private JButton btnRefresh;
    private JScrollPane jsp;
    private JLabel lblProgress;
    private FileTree fileTree;

    private static FtpClient ftp;
    private boolean isCancelled;
    private String desPath="";

    public static void main(String[] args) {
        clientGUI gui = new clientGUI();
        gui.setContentPane(new clientGUI().mainPanel);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.pack();
        gui.setLocationRelativeTo(null);
        gui.setVisible(true);
        gui.setResizable(false);
    }

    public clientGUI() {
        isCancelled=false;
        lblDestDir.setText(FileSystemView.getFileSystemView() .getHomeDirectory().getAbsolutePath());
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String host = txtAddr.getText();
                String username = txtUsername.getText();
                String password = String.valueOf(passwordField.getPassword());
                if (host.length() == 0) {
                    JOptionPane.showMessageDialog(null, "Hostname cannot be empty", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (username.length() == 0) {
                    JOptionPane.showMessageDialog(null, "Username cannot be empty", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (password.length() == 0) {
                    JOptionPane.showMessageDialog(null, "Password cannot be empty", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ftp = new FtpClient(host, username, password);
                try {
                    ftp.connect();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(null, "Connect successfully", "Message", JOptionPane.PLAIN_MESSAGE);
                showFiles();
            }
        });

        btnDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = fileTree.getFileName();
                String filePath = fileTree.getFilePath();
                if (fileName.equals("")) {
                    JOptionPane.showMessageDialog(null, "Please select a file to download", "Eessage", JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                String desDic = lblDestDir.getText();
                desPath = desDic + "/" + fileName;
                File file=new File(desPath);
                if(file.exists()){
                    int n=JOptionPane.showConfirmDialog(null,"File exists. Do you want to overide it?","Message",JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE);
                    if(n==JOptionPane.NO_OPTION){
                        btnDownload.setEnabled(true);
                        btnDownload.setText("Download");
                        return;
                    }
                    file.delete();
                }
                btnDownload.setEnabled(false);
                btnDownload.setText("Downloading");
                int size;
                try {
                    size = ftp.getSize(filePath);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                progBar.setMinimum(0);
                progBar.setMaximum(size);
                progBar.setValue(0);
                panelBottom.updateUI();
                new Thread(){
                    @Override
                    public void run() {
                        try{
                            ftp.download(filePath,fileName,desDic);
                        }catch (Exception ex) {
                            if(!isCancelled)
                                JOptionPane.showMessageDialog(null, "Cannot download the file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            else{
                                try {
                                    ftp.getCommandIn().readLine();
                                }catch(Exception exp){

                                }
                                isCancelled=false;
                            }
                        }
                        btnDownload.setEnabled(true);
                        btnDownload.setText("Download");
                    }
                }.start();
                ProgressThread progress = new ProgressThread(progBar, desPath, size);
                progress.start();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCancelled = true;
                try {
                    ftp.cancel();
                } catch (Exception ex) {
                    System.out.println("Cannot cancel: " + ex.getMessage());
                }
                File file = new File(desPath + ".download");
                if (file.exists()) {
                    file.delete();
                    JOptionPane.showMessageDialog(null, "Mission cancelled", "Message", JOptionPane.PLAIN_MESSAGE);
                    btnDownload.setText("Download");
                    btnDownload.setEnabled(true);
                }
            }
        });

        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Please choose a file");
                chooser.setMultiSelectionEnabled(false);//最多只能选一个文件
                FileNameExtensionFilter filter = new FileNameExtensionFilter("for test(*.java, *.py)", "java", "py");
                chooser.setFileFilter(filter);
                chooser.showOpenDialog(btnBrowse);
//                File f = chooser.getSelectedFile();
//                try {
//
//                }
//                catch (FileNotFoundException e){
//                    e.printStackTrace();
//                }

            }
        });

        btnChoose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Please choose a directory");
                chooser.setApproveButtonText("OK");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("directory", "./");
                chooser.setFileFilter(filter);
//                chooser.showOpenDialog(btnChoose);
                if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(btnChoose)) {
                    String path = chooser.getSelectedFile().getPath();
                    lblDestDir.setText(path);
                }
            }
        });
    }

    public void showFiles() {
        panelCenter.remove(jsp);
        fileTree = new FileTree(ftp);
        FileTreeModel model = new FileTreeModel(new DefaultMutableTreeNode(new FileNode("/", true, false)), ftp);
        fileTree.setModel(model);
        fileTree.setCellRenderer(new FileTreeRenderer());
        jsp = new JScrollPane(fileTree);
        panelCenter.add(jsp);
        panelCenter.updateUI();
    }
}