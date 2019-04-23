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
    private JLabel lblDirPath;
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
    private JLabel lblProgress;
    private JPanel panelCenterMid;
    private JPanel panelCenterMidLeft;
    private JPanel panelCenterMidRight;
    private JScrollPane jspLocal;
    private JScrollPane jspFTP;
    private JLabel lblFilePath;
    private FileTree fileTree;
    private LocalFileTree localFileTree;

    private static FtpClient ftp;
    private String desPath = "";
    private ProgressThread progressThread;
    private DownloadThread downloadThread;
    private UploadThread uploadThread;


    public JLabel getLblFilePath() {
        return lblFilePath;
    }

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
        lblDestDir.setText(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath());
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
                File file = new File(desPath);
                if (file.exists()) {
                    int n = JOptionPane.showConfirmDialog(null, "File exists. Do you want to overide it?", "Message", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (n == JOptionPane.NO_OPTION) {
                        btnDownload.setEnabled(true);
                        btnDownload.setText("Download");
                        return;
                    }
                    file.delete();
                }
                btnPause.setEnabled(true);
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
                progressThread = new ProgressThread(progBar, desPath, size);
                downloadThread = new DownloadThread(ftp, filePath, fileName, desDic, size, btnDownload);
                progressThread.start();
                downloadThread.start();
            }
        });

        btnUpload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = localFileTree.getFileName();
                String filePath = localFileTree.getFilePath();
                if (fileName.equals("")) {
                    JOptionPane.showMessageDialog(null, "Please select a file to download", "Eessage", JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                String desDic = lblDestDir.getText();
                desPath = desDic + "/" + fileName;
                File file = new File(desPath);
                if (file.exists()) {
                    int n = JOptionPane.showConfirmDialog(null, "File exists. Do you want to overide it?", "Message", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (n == JOptionPane.NO_OPTION) {
                        btnUpload.setEnabled(true);
                        btnUpload.setText("Upload");
                        return;
                    }
                    file.delete();
                }
                btnPause.setEnabled(true);
                btnUpload.setEnabled(false);
                btnUpload.setText("Uploading");
                int size;
                try {
                    size = ftp.getSize(filePath);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);
                    return;
                }
//                progBar.setMinimum(0);
//                progBar.setMaximum(size);
//                progBar.setValue(0);
                panelBottom.updateUI();
                progressThread = new ProgressThread(progBar, desPath, size);
                uploadThread = new UploadThread(ftp, filePath, fileName, desDic, size, btnUpload);
                progressThread.start();
                uploadThread.start();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = new File(desPath + ".download");
                if (!file.exists())
                    return;
                progressThread.interrupt();
                downloadThread.setCancelled(true);
                try {
                    ftp.cancel();
                } catch (Exception ex) {
                    System.out.println("Cannot cancel: " + ex.getMessage());
                }
                file.delete();
                progBar.setValue(0);
                JOptionPane.showMessageDialog(null, "Mission cancelled", "Message", JOptionPane.PLAIN_MESSAGE);
                btnDownload.setText("Download");
                btnDownload.setEnabled(true);
                btnPause.setEnabled(true);
            }
        });

        btnPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progressThread.interrupt();
                downloadThread.setCancelled(true);
                try {
                    ftp.cancel();
                } catch (Exception ex) {
                    System.out.println("Cannot pause: " + ex.getMessage());
                }
                btnPause.setEnabled(false);
                btnDownload.setText("Download");
                btnDownload.setEnabled(true);
            }
        });

        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Please choose a directory");
                //chooser.setMultiSelectionEnabled(false);//最多只能选一个文件
                chooser.setApproveButtonText("OK");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("directory", "./");
                chooser.setFileFilter(filter);
                if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(btnBrowse)) {
                    String path = chooser.getSelectedFile().getPath();
                    lblDirPath.setText(path);
                    showLocalFiles(path);
                }
            }
        });

        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFiles();
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
                if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(btnChoose)) {
                    String path = chooser.getSelectedFile().getPath();
                    lblDestDir.setText(path);
                }
            }
        });
    }

    public void showFiles() {
        panelCenterMidLeft.remove(jspFTP);
        fileTree = new FileTree(ftp);
        FileTreeModel model = new FileTreeModel(new DefaultMutableTreeNode(new FileNode("/", true, false)), ftp);
        fileTree.setModel(model);
        fileTree.setCellRenderer(new FileTreeRenderer());
        jspFTP = new JScrollPane(fileTree);
        panelCenterMidLeft.add(jspFTP);
        panelCenterMidLeft.updateUI();
    }

    public void showLocalFiles(String path) {
        panelCenterMidRight.remove(jspLocal);
        localFileTree = new LocalFileTree(path, this);
        jspLocal = new JScrollPane(localFileTree);
//        lblFilePath = new JLabel(localFileTree.getFilePath());
//        lblFilePath = new JLabel();
        panelCenterMidRight.add(jspLocal);
        panelCenterMidRight.updateUI();
    }

}