import javax.swing.*;
import java.io.File;

public class Progress extends Thread{
    private JProgressBar progBar;
    private String desFile;
    private int size;

    public Progress(JProgressBar progBar,String desFile,int size){
        this.progBar=progBar;
        this.desFile=desFile;
        this.size=size;
    }

    public void run(){
        int currentSize=0;
        File file=new File(desFile+".download");
        do{
            size=(int)file.length();
            progBar.setValue(size);
        } while (currentSize<size);
    }
}
