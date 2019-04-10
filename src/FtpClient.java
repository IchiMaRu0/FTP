import java.io.*;
import java.net.Socket;
import java.util.*;

public class FtpClient {
    private String username;
    private String password;
    private BufferedReader commandIn;
    private BufferedWriter commandOut;
    private String host;
    private int port = 21;
    private static final int PORT = 21;

    public FtpClient(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public BufferedReader getCommandIn() {
        return commandIn;
    }

    public BufferedWriter getCommandOut() {
        return commandOut;
    }

    public void connect() throws Exception {
        //create socket
        Socket socket = new Socket(host, PORT);
        commandIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        commandOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String msg = commandIn.readLine();
        while (!msg.startsWith("220"))
            msg = commandIn.readLine();
        //send username
        commandOut.write("USER " + username + "\r\n");
        commandOut.flush();
        msg = commandIn.readLine();
        if (!msg.startsWith("331"))
            throw new Exception("Incorrect username or password");
        //send password
        commandOut.write("PASS " + password + "\r\n");
        commandOut.flush();
        msg = commandIn.readLine();
        if (!msg.startsWith("230"))
            throw new Exception("Incorrect username or password");
    }

    public void toPASV() throws Exception {
        String msg;
        //change to passive mode
        commandOut.write("PASV\r\n");
        commandOut.flush();
        msg = commandIn.readLine();
        if (!msg.startsWith("227"))
            throw new Exception("Cannot change to passive mode");
        //get the port
        int start = msg.indexOf('(');
        int end = msg.indexOf(')');
        msg = msg.substring(start, end);
        String[] nums = msg.split(",");
        port = Integer.parseInt(nums[4]) * 256 + Integer.parseInt(nums[5]);
    }

    public List<String[]> getFiles() throws Exception {
        toPASV();
        commandOut.write("LIST\r\n");
        commandOut.flush();
        commandIn.readLine();
        Socket dataSocket = new Socket(host, port);
        BufferedReader dataIn = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        String data = dataIn.readLine();
        List<String[]> files = new ArrayList<>();
        while (data != null) {
            String[] split = data.split(" ");
            String fileName = split[split.length - 1];
            String[] fileInfo = new String[2];
            fileInfo[0] = fileName;
            fileInfo[1] = "0";
            if (data.contains("<DIR>"))
                fileInfo[1] = "1";
            files.add(fileInfo);
            data = dataIn.readLine();
        }
        commandIn.readLine();
        dataIn.close();
        dataSocket.close();
        return files;
    }

    public int getSize(String s) throws Exception {
        commandOut.write("SIZE " + s + "\r\n");
        commandOut.flush();
        String msg = commandIn.readLine();
        System.out.println(msg);
        if (!msg.startsWith("213"))
            throw new Exception("Cannot get the size of " + s);
        int size;
        try {
            size = Integer.parseInt(msg.substring(msg.indexOf(" ") + 1));
        } catch (Exception ex) {
            throw new Exception("Cannot get the size of " + s);
        }
        return size;
    }

    public void changeDir(String s) throws Exception {
        commandOut.write("CWD " + s + "\r\n");
        commandOut.flush();
        String msg = commandIn.readLine();
        if (!msg.startsWith("250"))
            throw new Exception("Cannot change to the named director");
    }

    public void changeUp() throws Exception {
        commandOut.write("CDUP\r\n");
        commandOut.flush();
        String msg = commandIn.readLine();
        if (!msg.startsWith("250"))
            throw new Exception("Cannot change to the parent director");
    }

    public void inAscii() throws Exception {
        commandOut.write("TYPE A\r\n");
        commandOut.flush();
        commandIn.readLine();
    }

    public void inBinary() throws Exception {
        commandOut.write("TYPE I\r\n");
        commandOut.flush();
        commandIn.readLine();
    }

    public void upload(String filePath) throws Exception {

    }

    public void download(String filePath, String fileName, String dicPath) throws Exception {
        toPASV();
        inBinary();
        File file = new File(dicPath, fileName + ".download");
        if (file.exists()) {
            long size = file.length();
            commandOut.write("REST " + size + "\r\n");
            commandOut.flush();
            commandIn.readLine();
        }
        commandOut.write("RETR " + filePath + "\r\n");
        commandOut.flush();
        commandIn.readLine();
        Socket dataSocket = new Socket(host, port);
        BufferedInputStream dataInput = new BufferedInputStream(dataSocket.getInputStream());
        FileOutputStream dataOut = new FileOutputStream(file, true);
        int n;
        byte[] buffer = new byte[1024];
        while ((n = dataInput.read(buffer, 0, 1024)) > 0)
            dataOut.write(buffer, 0, n);
        commandIn.readLine();
        file.renameTo(new File(dicPath + '/' + fileName));
        dataInput.close();
        dataOut.close();
        dataSocket.close();
    }
}
