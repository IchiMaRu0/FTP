package GUI;

import javax.swing.*;

public class clientGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("clientGUI");
        frame.setContentPane(new clientGUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private JPanel mainPanel;
    private JPanel panelTop;
    private JTextArea txtAddr;
    private JTextArea txtUsername;
    private JButton btnLogin;
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
}
