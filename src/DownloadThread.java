import javax.swing.*;

public class DownloadThread extends Thread {
    private FtpClient ftp;
    private String filePath;
    private String fileName;
    private String desDic;
    private int size;
    private boolean isCancelled;
    private JButton btnDownload;

    public DownloadThread(FtpClient ftp, String filePath,String fileName,String desDic,int size,JButton btnDownload){
        this.ftp=ftp;
        this.filePath=filePath;
        this.fileName=fileName;
        this.desDic=desDic;
        this.size=size;
        this.btnDownload=btnDownload;
        isCancelled=false;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    public void run() {
        try{
            ftp.download(filePath,fileName,desDic,size);
            btnDownload.setEnabled(true);
            btnDownload.setText("Download");
        }catch (Exception ex) {
            if(!isCancelled)
                JOptionPane.showMessageDialog(null, "Cannot download the file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            else{
                try {
                    ftp.getCommandIn().readLine();
                }catch(Exception exp){

                }
                isCancelled=false;
                return;
            }
        }
    }
}
