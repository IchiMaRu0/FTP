import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

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
    private JTable tblInfo;
    private JScrollPane jsp;
    private JLabel lblProgress;

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
}