import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import utils.Log;
import utils.MimeTypeFactory;
import utils.MyBase64;

public class SMTP {
    final int SMTP_PORT = 25;
    String smtp_server = "";
    String my_email_addr ="";
    String my_email_pass ="";
    Socket smtp;
    InputStream smtp_in;
    OutputStream smtp_out;
    private final static String BOUNDARY;//MIME分格符
    private final static String CHARSET;//虚拟机的默认编码
    boolean login_suc = false; //是否已成功登录

    static
    {
        BOUNDARY="Boundary-=_hMbeqwnGNoWeLsRMeKTIPeofyStu";
        CHARSET=Charset.defaultCharset().displayName();
    }


    //SMPT构造
    public SMTP(String smtp_server,String my_email_addr,String my_email_pass){
        this.smtp_server = smtp_server;
        this.my_email_addr = my_email_addr;
        this.my_email_pass = my_email_pass;

        loginSmtpServer();
    }

    /**
     * 建立连接、认证身份
     * */
    public void loginSmtpServer(){
        try {
            //和smtp服务器创建连接
            smtp = new Socket(smtp_server,SMTP_PORT);
            //从服务器接收信息
            smtp_in = smtp.getInputStream();
            //要给服务器发送信息
            smtp_out = smtp.getOutputStream();
            Log.log("\r\n========start=========="+new SimpleDateFormat("HH-mm-ss", Locale.CHINA).format(new Date())+
                    "=========start===============\r\n");
            reqAndRes("HELO "+my_email_addr,true);
            getNewLine();
            getRes();
            reqAndRes("auth login\r\n",true);
            getRes();
            reqAndRes(MyBase64.getBASE64(my_email_addr),true);
            getNewLine();
            getRes();
            reqAndRes(MyBase64.getBASE64(my_email_pass),true);
            getNewLine();
            getRes();
            //Client:MAIL FROM
            reqAndRes("MAIL FROM:"+my_email_addr,true);
            getLoginRes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接释放
     * */
    public void quitSmtp(){
        try {
            reqAndRes("QUIT",true);
            smtp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *邮件发送
     * 把Mail对象发送出去
     * */
    public void send(Mail mail){
        String filedata = null;

        try{
            //Client:MAIL FROM
            //reqAndRes("MAIL FROM:"+my_email_addr,true);
            //getRes();
            //Client:RCPT TO
            for (int i = 0; i < mail.getTo_list().size(); i++) {
                reqAndRes("RCPT TO:" + mail.getTo_list().get(i),true);
                getNewLine();
                getRes();
            }
            //Client:DATA
            reqAndRes("DATA",true);
            getNewLine();
            getRes();

            String pre_data = "From: " + mail.getFrom() + "\r\n" + "To: " + mail.getTo() + "\r\n" + "Subject: " + mail.getSubject()
                    + "\r\n" + "MIME-Version: 1.0" +"\r\n";
            reqAndRes(pre_data,true);
            //发送邮件信息：纯文本时
            if(!mail.isMime_boundary()) {
                reqAndRes("Content-Type: text/plain; charset=\"" + CHARSET + "\"" + "\r\n"
                        + "Content-Transfer-Encoding: base64" + "\r\n" + MyBase64.getBASE64(mail.getContent()),true);
                getNewLine();
                getRes();
            }

            //发送邮件信息：带有附件时
            if(mail.isMime_boundary()) {
                File[] attachments = new File[mail.getAttachments().size()];
                for(int i =0;i<attachments.length;i++){
                    attachments[i] = mail.getAttachments().get(i);
                    System.out.println("attachments[i]="+attachments[i]);
                }

                reqAndRes("Content-Type: multipart/mixed; BOUNDARY=\""+BOUNDARY+"\"" + "\r\n"
                        + "Content-Transfer-Encoding: base64" + "\r\n" +
                                "--"+BOUNDARY + "\r\n"+"Content-Type: text/plain; charset=\""+CHARSET+"\""+
                        "\r\n"+"Content-Transfer-Encoding: base64"+ "\r\n"+
                        MyBase64.getBASE64(mail.getContent())+"\r\n",true);

                getFileData(attachments);
            }

            reqAndRes("."+"\r\n",true);
            getRes();
            Log.log("=========end============end======"+new SimpleDateFormat("HH-mm-ss", Locale.CHINA).format(new Date())
            +"======end====\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端与服务端交互
     * */
    private void reqAndRes(String command,boolean w_log) throws IOException{
        if(w_log) Log.log("client_req ---> " + command+"\r\n");
        System.out.println("client_req ---> " + command);
        smtp_out.write(command.getBytes());
        smtp_out.flush();
    }
    public void getRes() throws IOException{
        String res = new BufferedReader(new InputStreamReader(smtp_in)) . readLine();
        Log.log("server_res ---> " + res+"\r\n");
        System.out.println("server_res ---> " + res);
    }

    public void getLoginRes() throws IOException{
        String res = new BufferedReader(new InputStreamReader(smtp_in)) . readLine();
        Log.log("server_res ---> " + res+"\r\n");
        System.out.println("server_res ---> " + res);
        if(res.substring(0, 3).equals("235")) this.login_suc = true;
    }

    private void getNewLine() throws IOException {
        smtp_out.write("\r\n".getBytes());
        Log.log("\r\n");
    }

    public void getFileData(File[] attachments) throws IOException {
        RandomAccessFile attachment=null;
        int fileIndex=0;
        String fileName;
        int k;
        byte[] data=new byte[54];
        String res ="";
        try {
            for(;fileIndex<attachments.length;fileIndex++){
                fileName = attachments[fileIndex].getName();
                System.out.println("filename==~~~"+fileName);
                attachment = new RandomAccessFile(attachments[fileIndex],"r");
                res = "--"+BOUNDARY +"\r\n" +"Content-Type: "+ MimeTypeFactory.getMimeType(fileName.indexOf(".")==-1?"*":fileName.substring(fileName.lastIndexOf(".")+1))+"; name=\""+(MyBase64.getBASE64(fileName))+"\""
                +"\r\n" +"Content-Transfer-Encoding: base64"+"\r\n"+"Content-Disposition: attachment; filename=\""+fileName+"\"" +"\r\n";
                reqAndRes(res,true);
                do{
                    k=attachment.read(data,0,54);
                    if(k==-1) break;
                    reqAndRes(MyBase64.getBASE64(data,0,k),false);
                }while(k==54);
            }
        } catch (FileNotFoundException e) {
            Log.log("file not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.log("IOException");
            e.printStackTrace();
        }
        reqAndRes("\r\n"+"--"+BOUNDARY+"--"+"\r\n",true);
    }

    @Override
    public String toString(){
        String info = "server="+smtp_server +"my_addr="+ my_email_addr+ "pass:"+my_email_pass;
        return info;
    }

    public void setMy_email_pass(String my_email_pass) {
        this.my_email_pass = my_email_pass;
    }
}
