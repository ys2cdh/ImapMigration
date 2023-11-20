package com.funnysalt.bean;


import com.funnysalt.util.AES256;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

//키 값은 이메일 주소
@Component
public class UserInfoFile {

    private HashMap<String,String> mapUserInfo = new HashMap<String,String>();
    File userInfoFile;

    public void init(String strFilePath){
 
        userInfoFile = new File(strFilePath);
        if (userInfoFile.exists() && 0 < userInfoFile.length()) {
            readFile();
        }
    }

    public HashMap<String, String> getMapUserInfo() {
        return mapUserInfo;
    }

    public synchronized void addUSourceUserInfo(String email,String pw){
        if (mapUserInfo.containsKey(email)){
            return;
        }

        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);

            // statement 객체 생성
            statement = con.createStatement();
            // RDB와 통신
            statement.executeUpdate("UPDATE EMAIL_USER SET source_email='"+email+"', source_pw="+pw);


        } catch (SQLException se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }

    public synchronized void addUserInfo(String email,String pw){
        if (mapUserInfo.containsKey(email)){
            return;
        }

        BufferedWriter writer=null;
        try {
            pw = new AES256().encrypt(pw);
            String value = email + " " + pw + " 0 0"; // id  , pw , 전체 메일 수 , 다운로드한 메일 수 , 업로드한 메일 수
            mapUserInfo.put(email,value);

            writer = new BufferedWriter(new FileWriter(userInfoFile));
            for( Map.Entry<String, String> entry : mapUserInfo.entrySet() ){
                String strKey = entry.getKey();
                String strValue = entry.getValue();
                writer.write(strValue+"\r\n");
                System.out.println( strKey +":"+ strValue );
            }
            writer.flush();
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            if ( null != writer) {try {writer.close();writer=null;}catch (Exception e){}}
        }
    }

    private void readFile(){
        BufferedReader reader = null;
        try {
           reader = new BufferedReader(new FileReader(userInfoFile));
           String line = null;
           while ((line = reader.readLine()) != null) {
               String []strLines = line.split(" ");
               // email , pw , email all count , write email count
               if ( 4 == strLines.length ){
                   mapUserInfo.put(strLines[0],line);
               }
           }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if ( null != reader) {try {reader.close();reader=null;}catch (Exception e){}}
        }
    }
}
