import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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
    private JTree fileTree;

    private static FtpClient ftp;

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
        fileTree=new JTree();
        FileTreeModel model=new FileTreeModel(new DefaultMutableTreeNode(new FileNode("/",true,false)),ftp);
        fileTree.setModel(model);
        fileTree.setCellRenderer(new FileTreeRenderer());
        fileTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode lastTreeNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                FileNode fileNode = (FileNode) lastTreeNode.getUserObject();
                if(!fileNode.isDir)
                    return;
                if (!fileNode.isInit) {
                    String s = "";
                    for (Object o : lastTreeNode.getUserObjectPath())
                        s += "/"+((FileNode) o).name;
                    try {
                        ftp.changeDir(s);
                        List<String[]> files = ftp.getFiles();
                        for (String[] fileInfo : files)
                            lastTreeNode.add(new DefaultMutableTreeNode(new FileNode(fileInfo[0],fileInfo[1].equals("1"),false)));
                    } catch (Exception ex) {

                    }
                    DefaultTreeModel treeModel = (DefaultTreeModel) fileTree.getModel();
                    treeModel.nodeStructureChanged(lastTreeNode);
                    System.out.println(s);
                }
                fileNode.isInit = true;
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });
        jsp = new JScrollPane(fileTree);
        panelCenter.add(jsp);
        panelCenter.updateUI();
    }
}

class FileNode {
    public String name;
    public boolean isDir;
    public boolean isInit;

    public FileNode(String name, boolean isDir, boolean isInit) {
        this.name = name;
        this.isDir = isDir;
        this.isInit = isInit;
    }
}

class FileTreeRenderer extends DefaultTreeCellRenderer {
    public FileTreeRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(javax.swing.JTree tree,
                                                  java.lang.Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        FileNode fileNode = (FileNode) node.getUserObject();
        String name = fileNode.name;
        File file = null;
        try {
            if (name.contains(".") && !name.endsWith("."))
                file = File.createTempFile("icon", name.substring(name.indexOf(".")));
            else
                file = File.createTempFile("icon", "");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        label.setText(name);
        final JFileChooser fc = new JFileChooser();
        label.setIcon(fc.getUI().getFileView(fc).getIcon(file));
        label.setOpaque(false);
        file.delete();
        return label;
    }
}

class FileTreeModel extends DefaultTreeModel {
    public FileTreeModel(TreeNode root,FtpClient ftp) {
        super(root);
        try {
            List<String[]> files = ftp.getFiles();
            for (String[] fileInfo : files)
                ((DefaultMutableTreeNode)root).add(new DefaultMutableTreeNode(new FileNode(fileInfo[0], fileInfo[1].equals("1"), false)));
        } catch (Exception ex) {

        }
    }
    @Override
    public boolean isLeaf(Object node) {
        DefaultMutableTreeNode treeNode=(DefaultMutableTreeNode)node;
        FileNode fileNode=(FileNode)treeNode.getUserObject();
        return !fileNode.isDir;
    }
}