import javax.swing.*;
import java.io.File;

public class ProgressThread extends Thread{
    private JProgressBar progBar;
    private String desFile;
    private int size;

    public ProgressThread(JProgressBar progBar, String desFile, int size){
        this.progBar=progBar;
        this.desFile=desFile;
        this.size=size;
    }

    public void run(){
        int currentSize=0;
        File file=new File(desFile+".download");
        do{
            if(this.isInterrupted())
                return;
            currentSize=(int)file.length();
            progBar.setValue(currentSize);
        } while (currentSize<size);
        JOptionPane.showMessageDialog(null,"Download successfully","Message",JOptionPane.PLAIN_MESSAGE);
        progBar.setValue(0);
    }
}
