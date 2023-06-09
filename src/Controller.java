import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    static Scanner input = new Scanner(System.in);
    ArrayList<String> list = new ArrayList<>();
    ArrayList<File> files = new ArrayList<>();
    String smtp_server="";
    String pop3_server="";
    String from;
    String ifattachment;

    public Controller() {
        System.out.println("Welcome to my Simple Smtp Client, you can input 【exit】to end");
    }

    public static void main(String args[]) {
        Controller controller = new Controller();
        while(true) {
            System.out.println("input Your EMail Address:");
            controller.from = input.nextLine().trim();
            if(controller.checkMail(controller.from)) break;
        }
        String postfix = parseUrl(controller.from);
        System.out.println("smtp server=smtp."+postfix+"? input yes or no");
        if(input.nextLine().equals("no")){
            System.out.println("please input correct smtp_server");
            controller.smtp_server = input.nextLine();
        }
        else{
            controller.smtp_server = "smtp."+postfix;
            System.out.println("the auto smtp_server:"+controller.smtp_server);
        }

        System.out.println("pop3server=pop3."+postfix+"? input yes or no");
        if(input.nextLine().equals("no")){
            System.out.println("please input correct smtp_server");
            controller.pop3_server = input.nextLine();
        }
        else{
            controller.pop3_server = "smtp."+postfix;
            System.out.println("the auto pop3_server:"+controller.pop3_server);
        }

        System.out.println("input your email Password:");
        String password = input.nextLine();
        SMTP smtp = new SMTP(controller.smtp_server, controller.from, password);
        while(smtp.login_suc == false){
            System.out.println("the [password] is not correct,please [input again]");
            password = input.nextLine();
            smtp.setMy_email_pass(password);
            smtp.loginSmtpServer();
        }
        while(true) {
            System.out.println("【1】:Send Email,【2】:Check Email，【3】exit 【input 1or2or3】");
            String choice = input.nextLine();
            if (choice.equals("1")) {
                while(true) {
                    System.out.println("input To Email Address (if you have several recipients,please split them with';')");
                    String to_list_string = input.nextLine();
                    if(controller.regexToList(to_list_string)) break;
                }
                System.out.println("input your email Subject:");
                String subject = input.nextLine();
                System.out.println("input your email Content:");
                String content = input.nextLine();
                while(true) {
                    System.out.println("Any attachments?input 'yes' or 'no'");
                    controller.ifattachment = input.nextLine();
                    if((controller.ifattachment).equals("yes")||(controller.ifattachment).equals("no")) break;
                    else System.out.println("please input 'yes' or 'no'");
                }
                if (controller.ifattachment.equals("yes")) {
                    System.out.println("Input FilePath(./test.pdf),if you have several attachments,split them with';'");
                    String files = input.nextLine();
                    controller.regexFiles(files);
                    Mail mail = new Mail(controller.from, controller.list, subject, content, controller.files);
                    //SMTP smtp = new SMTP(controller.smtp_server, controller.from, password);
                    smtp.send(mail);
                    smtp.quitSmtp();
                } else{
                    Mail mail = new Mail(controller.from, controller.list, subject, content, null);
                    //SMTP smtp = new SMTP(controller.smtp_server, controller.from, password);
                    smtp.send(mail);
                    smtp.quitSmtp();
                }
            }
            if (choice.equals("2")) {
                try {
                    POP3 pop3Client = new POP3(controller.pop3_server, 110, controller.from, password);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (choice.equals("3")) break;
        }
    }

    private boolean regexToList(String to_list_string){
        Pattern p = Pattern.compile("[;]");
        String[] r = p.split(to_list_string);
        for(int i =0;i<r.length;i++){
            if(!checkMail(r[i])) {
                return false;}
        }
        Matcher m = p.matcher(to_list_string);
        Collections.addAll(this.list,r);
        this.list.forEach((e) -> {
            System.out.print("send to :"+e +"\r\n");
        });
        return true;
    }

    private void regexFiles(String files_string){
        Pattern pa = Pattern.compile("[;]");
        String[] rf = pa.split(files_string);
        File[] files_res = new File[rf.length];
        for(int i =0;i<rf.length;i++){
            files_res[i] = new File(rf[i].trim());
        }
        Collections.addAll(this.files,files_res);
        this.files.forEach((e) -> {
            System.out.print("File:"+e +"\r\n");
        });
    }

    private boolean checkMail(String from){
        if (from == null)
            return false;
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(from);
        if (m.matches())
            return true;
        else{
            System.out.println("your email address is wrong.");
            return false;
        }
    }

    /**
     * 分析邮箱域名。
     * @param address E-Mail地址
     * @return 邮箱域名
     */
    private static String parseUrl(String address)
    {
        return address.substring(address.lastIndexOf('@')+1);
    }
}