package com.zk;

import java.io.*;

/**
 * Created by Ken on 2016/9/2.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        File f = new File("E://1.txt");
        FileInputStream fi = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fi));

        while(br.read() != -1){
            String s = br.readLine().trim();
            s = s.replace("\"","'");
            if(s.startsWith("'")||s.startsWith("compile(")){
                if(s.length() < 11){
                    continue;
                }
                s = s.replaceFirst(":","&");
                System.out.println("<dependency>");
                System.out.println("<groupId>"+s.substring(s.indexOf("'")+1, s.indexOf("&"))+"</groupId>");
                System.out.println("<artifactId>"+s.substring(s.indexOf("&")+1,s.indexOf(":"))+"</artifactId>");
                System.out.println("<version>"+s.substring(s.indexOf(":")+1,s.lastIndexOf("'"))+"</version>");
                System.out.println("</dependency>");
                System.out.println("");
            }else if(s.startsWith("compile") && s.contains("group")){
                System.out.println("<dependency>");
                System.out.println("<groupId>"+s.substring(s.indexOf("'")+1, s.indexOf("',"))+"</groupId>");
                s = s.substring(s.indexOf("',")+2, s.length());
                System.out.println("<artifactId>"+s.substring(s.indexOf("'")+1,s.indexOf("',"))+"</artifactId>");
                s = s.substring(s.indexOf("',")+2, s.length());
                System.out.println("<version>"+s.substring(s.indexOf("'")+1,s.lastIndexOf("'"))+"</version>");
                System.out.println("</dependency>");
                System.out.println("");

            }
        }
    }
}
