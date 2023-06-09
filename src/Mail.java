import java.io.File;
import java.util.ArrayList;

public class Mail {
    String from = ""; //发送方地址
    ArrayList<String> to_list;  //发送人（一个或多个）
    String subject = "";     //主题
    String content_type="text/html";  //内容类型
    String content_Transfer_Encoding="base64"; //编码方式
    String content = "";
    ArrayList<File> attachments;
    boolean mime_boundary;

    public Mail(String from, ArrayList<String> to_list, String subject, String content,ArrayList<File> attachments) {
        this.from = from;
        this.to_list = to_list;
        this.subject = subject;
        this.content = content;
        this.attachments = attachments;
        this.mime_boundary = attachments!=null && attachments.size()>0;
        System.out.println("boundary??"+this.mime_boundary);
    }

    public String getTo(){
        String to = "";
        for(int i = 0; i< to_list.size();i++){
            to += to_list.get(i);
        }
        return to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public ArrayList<String> getTo_list() {
        return to_list;
    }

    public void setTo_list(ArrayList<String> to_list) {
        this.to_list = to_list;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMime_boundary(boolean mime_boundary) {
        this.mime_boundary = mime_boundary;
    }

    public ArrayList<File> getAttachments() {
        return attachments;
    }

    public boolean isMime_boundary() {
        return mime_boundary;
    }

    public void setAttachments(ArrayList<File> attachments) {
        this.attachments = attachments;
    }
}
