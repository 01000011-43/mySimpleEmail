/**
 * @author can
 * 基于POP3协议的邮件接收功能
 * */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Log;
public class POP3 {

    final int POP3_PORT = 110;
    private Socket socket = null;
    private boolean debug=false;
    String server="";//POP3服务器地址
    static Scanner input = new Scanner(System.in);

    /*构造函数*/
    public POP3(String server,int port,String user,String password) throws UnknownHostException, IOException{
        try{
            socket=new Socket(server,port);//在新建socket的时候就已经与服务器建立了连接
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            Log.log("\r\n===============pop3==start===="+
                    new SimpleDateFormat("HH-mm-ss", Locale.CHINA).format(new Date())+"" +
                    "==========连接建立====\r\n");
            System.out.println("建立连接！");
        }
        recieveMail(user,password);
    }


    //接收邮件程序
    public boolean recieveMail(String user,String password){
        try {
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            user(user,in,out);//输入用户名
            pass(password,in,out);//输入密码
            stat(in,out);
            list(in,out);
            System.out.println("input the number you want to get details");
            int number = input.nextInt();
            retr(number,in,out);
            quit(in,out);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //得到服务器返回的一行命令
    public String getReturn(BufferedReader in){
        String line="";
        try{
            line=in.readLine();
            Log.log("pop3 server---->:"+line+"\r\n");
            if(debug){
                System.out.println("pop3 server---->:"+line+"\r\n");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return line;
    }

    //从返回的命令中得到第一个字段,也就是服务器的返回状态码(+OK或者-ERR)
    public String getResult(String line){
        StringTokenizer st=new StringTokenizer(line," ");
        return st.nextToken();
    }

    //发送命令
    private String sendServer(String str,BufferedReader in,BufferedWriter out) throws IOException{
        out.write(str);//发送命令
        out.newLine();//发送空行
        out.flush();//清空缓冲区
        Log.log("pop3client---->:"+str+"\r\n");
        if(debug){
            System.out.println("pop3client---->:"+str);
        }
        return getReturn(in);
    }

    //user命令
    public void user(String user,BufferedReader in,BufferedWriter out) throws IOException{
        String result = null;
        result=getResult(getReturn(in));//先检测连接服务器是否已经成功
        if(!"+OK".equals(result)){
            Log.log("failed to connect the pop3 server\r\n");
            throw new IOException("连接服务器失败!");
        }
        result=getResult(sendServer("user "+user,in,out));//发送user命令
        if(!"+OK".equals(result)){
            Log.log("username is wrong!\r\n");
            throw new IOException("用户名错误!");
        }
    }

    //pass命令
    public void pass(String password,BufferedReader in,BufferedWriter out) throws IOException{
        String result = null;
        result = getResult(sendServer("pass "+password,in,out));
        if(!"+OK".equals(result)){
            Log.log("password is wrong!\r\n");
            throw new IOException("密码错误!");
        }
    }

    //stat命令
    public int stat(BufferedReader in,BufferedWriter out) throws IOException{
        String result = null;
        String line = null;
        int mailNum = 0;
        line=sendServer("stat",in,out);
        StringTokenizer st=new StringTokenizer(line," ");
        result=st.nextToken();
        if(st.hasMoreTokens())
            mailNum=Integer.parseInt(st.nextToken());
        else{
            mailNum=0;
        }
        if(!"+OK".equals(result)){
            Log.log("stat failed!\r\n");
            throw new IOException("查看邮箱状态出错!");
        }
        Log.log("stat success,mail"+mailNum+"\r\n");
        System.out.println("共有邮件"+mailNum+"封");
        return mailNum;
    }

    //无参数list命令
    public void list(BufferedReader in,BufferedWriter out) throws IOException{
        String message = "";
        String line = null;
        line=sendServer("list",in,out);
        while(!".".equalsIgnoreCase(line)){
            message=message+line+"\n";
            line=in.readLine().toString();
        }
        System.out.println(message);
    }

    //带参数list命令
    public void list_one(int mailNumber ,BufferedReader in,BufferedWriter out) throws IOException{
        String result = null;
        result = getResult(sendServer("list "+mailNumber,in,out));
        if(!"+OK".equals(result)){
            Log.log("list is wrong!\r\n");
            throw new IOException("list错误!");
        }
    }

    //得到邮件详细信息
    public String getMessagedetail(BufferedReader in) throws UnsupportedEncodingException{
        String message = "";
        String line = null;
        try{
            line=in.readLine().toString();
            while(!".".equalsIgnoreCase(line)){
                message=message+line+"\n";
                line=in.readLine().toString();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return message;
    }

    //retr命令
    public void retr(int mailNum,BufferedReader in,BufferedWriter out) throws IOException, InterruptedException{
        String result = null;
        result=getResult(sendServer("retr "+mailNum,in,out));
        if(!"+OK".equals(result)){
            throw new IOException("接收邮件出错!");
        }
        System.out.println("第"+mailNum+"封");
        Log.log("retr the no."+mailNum+"email\r\n");
        System.out.println(getMessagedetail(in));
        Thread.sleep(3000);
    }

    //退出
    public void quit(BufferedReader in,BufferedWriter out) throws IOException{
        String result;
        result=getResult(sendServer("QUIT",in,out));
        if(!"+OK".equals(result)){
            Log.log("quit failed!\r\n");
            throw new IOException("未能正确退出");
        }
        Log.log("==========quit==========\r\n");
    }
}
