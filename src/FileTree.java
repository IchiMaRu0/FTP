import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class FileTree extends JTree {
    private FtpClient ftp;
    private StringBuilder filePath;
    private String fileName;

    public String getFilePath() {
        return filePath.toString();
    }

    public String getFileName() {
        return fileName;
    }

    public FileTree(FtpClient ftp) {
        this.ftp = ftp;
        filePath = new StringBuilder();
        fileName = "";
        addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode lastTreeNode = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                FileNode fileNode = (FileNode) lastTreeNode.getUserObject();
                if (!fileNode.isDir)
                    return;
                if (!fileNode.isInit) {
                    StringBuilder s = new StringBuilder();
                    for (Object o : lastTreeNode.getUserObjectPath())
                        s.append("/" + ((FileNode) o).name);
                    try {
                        ftp.changeDir(s.toString());
                        List<String[]> files = ftp.getFiles();
                        for (String[] fileInfo : files)
                            lastTreeNode.add(new DefaultMutableTreeNode(new FileNode(fileInfo[0], fileInfo[1].equals("1"), false)));
                    } catch (Exception ex) {

                    }
                    DefaultTreeModel treeModel = (DefaultTreeModel) getModel();
                    treeModel.nodeStructureChanged(lastTreeNode);
                }
                fileNode.isInit = true;
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

            }
        });

        addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode lastTreeNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                FileNode fileNode = (FileNode) lastTreeNode.getUserObject();
                filePath.setLength(0);
                if (!fileNode.isDir) {
                    fileName = fileNode.name;
                    for (Object o : lastTreeNode.getUserObjectPath())
                        filePath.append("/" + ((FileNode) o).name);
                    System.out.println(filePath.toString());
                } else {
                    filePath.setLength(0);
                    fileName = "";
                    System.out.println(filePath.toString());
                }
            }
        });
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
        if (fileNode.isDir) {
            label.setText(name);
            label.setOpaque(false);
            return label;
        }
        //so stupid
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
    public FileTreeModel(TreeNode root, FtpClient ftp) {
        super(root);
        try {
            List<String[]> files = ftp.getFiles();
            for (String[] fileInfo : files)
                ((DefaultMutableTreeNode) root).add(new DefaultMutableTreeNode(new FileNode(fileInfo[0], fileInfo[1].equals("1"), false)));
        } catch (Exception ex) {
            System.out.println("cannot get files");
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
        FileNode fileNode = (FileNode) treeNode.getUserObject();
        return !fileNode.isDir;
    }
}
