package utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * @author sandy
 *
 */


public class Log {
    static String today = new SimpleDateFormat("MM-dd-yyyy", Locale.CHINA).format(new Date());
    static String path = "./logs/"+today+".txt";
    static File file = new File(path);

    public static void log(String str){
        if(!file.exists()){
            try {
                PrintWriter newout = new PrintWriter(file);
                newout.write(str);
                newout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(file.exists()){
            try {
                FileWriter out = new FileWriter(file,true);
                out.write(str);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
