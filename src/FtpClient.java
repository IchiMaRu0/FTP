import java.io.*;
import java.net.Socket;

public class FtpClient {
    private String username;
    private String password;
    private BufferedReader commandIn;
    private BufferedWriter commandOut;
    private String host;
    private int port = 21;
    private static final int PORT = 21;

    //test
    public static void main(String[] args){
        FtpClient ftp=new FtpClient("192.168.1.102","test","123456");
        try{
            ftp.toPASV();
            ftp.getFiles();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public FtpClient(String host,String username, String password) {
        this.host=host;
        this.username=username;
        this.password=password;
        try {
            connect();
        }catch (Exception e){

        }
    }

    public void connect() throws Exception{
        //create socket
        Socket socket = new Socket(host, PORT);
        commandIn=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        commandOut=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String msg=commandIn.readLine();
        while(!msg.startsWith("220"))
            msg=commandIn.readLine();
        //send username
        commandOut.write("USER "+username+"\r\n");
        commandOut.flush();
        msg=commandIn.readLine();
        if(!msg.startsWith("331")) {
            //TODO
        }
        //send password
        commandOut.write("PASS "+password+"\r\n");
        commandOut.flush();
        msg=commandIn.readLine();
        if(!msg.startsWith("230")){
            //TODO
        }
        System.out.println(msg);
    }

    public void toPASV() throws Exception{
        String msg;
        //change to passive mode
        commandOut.write("PASV\r\n");
        commandOut.flush();
        msg=commandIn.readLine();
        if(!msg.startsWith("227")){
            //TODO
        }
        //get the port
        int start=msg.indexOf('(');
        int end=msg.indexOf(')');
        System.out.println(msg);
        msg=msg.substring(start,end);
        String[] nums=msg.split(",");
        port =Integer.parseInt(nums[4])*256+Integer.parseInt(nums[5]);
        System.out.println(port);
    }

    public void getFiles() throws Exception{
        commandOut.write("LIST\r\n");
        commandOut.flush();
        Socket dataSocket=new Socket(host,port);
        commandIn=new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        String data=commandIn.readLine();
        while(data!=null){
            System.out.println(data);
            data=commandIn.readLine();
        }
    }
}