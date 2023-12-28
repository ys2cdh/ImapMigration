package com.funnysalt.bean;


import com.funnysalt.service.ImapOneUserMigration;
import com.funnysalt.util.AES256;
import com.funnysalt.util.ActiveTasksThreadPool;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

//키 값은 이메일 주소
@Component
public class UserInfoFile {

    ActiveTasksThreadPool activeTasksThreadPool;
    private HashMap<String,String> mapSourceUserInfo = new HashMap<String,String>();
    private HashMap<String,String> mapTargetUserInfo = new HashMap<String,String>();
    File userInfoFile;

//    public void init(String strFilePath){
//
//        userInfoFile = new File(strFilePath);
//        if (userInfoFile.exists() && 0 < userInfoFile.length()) {
//            readFile();
//        }
//    }

    public HashMap<String, String> getSourceMapUserInfo() {
        return mapSourceUserInfo;
    }

    public synchronized void addSourceUserInfo(String email, String pw,long nTotalCount){


        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            pw = new AES256().encrypt(pw);
            String value = email + " " + pw + " 0 0"; // id  , pw , 전체 메일 수 , 다운로드한 메일 수 , 업로드한 메일 수

            if (mapSourceUserInfo.containsKey(email) && mapSourceUserInfo.get(email).equals(value)){
                return;
            }

            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);


            // statement 객체 생성
            statement = con.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM EMAIL_USER where source_email='"+email+"'");
            if ( resultSet.next() ) {
                statement.executeUpdate("UPDATE EMAIL_USER SET source_pw ='" + pw +"', total_eml_count ="+nTotalCount+ " where source_email ='"+email+"'");

            } else {
                 statement.executeUpdate("INSERT INTO EMAIL_USER (source_email,source_pw,total_eml_count,download_cml_count,up_eml_count,download_muid,migration_state) values ('" + email + "','" + pw+ "',"+ nTotalCount+ ",0,0,1,0)");
            }


            mapSourceUserInfo.put(email,value);
        } catch (Exception se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }


    public synchronized void addTargetUserInfo(String sourceEmail,String email,String pw){


        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            pw = new AES256().encrypt(pw);
            String value = email + " " + pw + " 0 0"; // id  , pw , 전체 메일 수 , 다운로드한 메일 수 , 업로드한 메일 수

            if (mapSourceUserInfo.containsKey(email) && mapSourceUserInfo.get(email).equals(value)){
                return;
            }

            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);


            // statement 객체 생성
            statement = con.createStatement();
            // RDB와 통신
            statement.executeUpdate("UPDATE EMAIL_USER SET target_email = '"+email+"', target_pw = '"+pw+"', migration_state=1  where source_email='"+sourceEmail+"'");


            mapTargetUserInfo.put(email,value);

            ImapOneUserMigration imapOneUserMigration = new ImapOneUserMigration(sourceEmail);
            activeTasksThreadPool.execute(imapOneUserMigration);
        } catch (Exception se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }

    public void read() {

        // pool 5개
        activeTasksThreadPool = new ActiveTasksThreadPool(5,5,10, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
//            pw = new AES256().encrypt(pw);
//            String value = email + " " + pw + " 0 0"; // id  , pw , 전체 메일 수 , 다운로드한 메일 수 , 업로드한 메일 수

            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);


            // statement 객체 생성
            statement = con.createStatement();
            // RDB와 통신
            resultSet = statement.executeQuery("SELECT * FROM EMAIL_USER WHERE migration_state !=2");

            while (resultSet.next()) {
                String strSourceEmail = resultSet.getString(1);
                String strSourcePW = resultSet.getString(2);
                strSourcePW = new AES256().decrypt(strSourcePW);

                activeTasksThreadPool.getActiveCount();

                ImapOneUserMigration imapOneUserMigration = new ImapOneUserMigration(strSourceEmail);
                activeTasksThreadPool.execute(imapOneUserMigration);

            }

        } catch (Exception se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }

//    public synchronized void addUserInfo(String email,String pw){
//        if (mapUserInfo.containsKey(email)){
//            return;
//        }
//
//        BufferedWriter writer=null;
//        try {
//            pw = new AES256().encrypt(pw);
//            String value = email + " " + pw + " 0 0"; // id  , pw , 전체 메일 수 , 다운로드한 메일 수 , 업로드한 메일 수
//            mapUserInfo.put(email,value);
//
//            writer = new BufferedWriter(new FileWriter(userInfoFile));
//            for( Map.Entry<String, String> entry : mapUserInfo.entrySet() ){
//                String strKey = entry.getKey();
//                String strValue = entry.getValue();
//                writer.write(strValue+"\r\n");
//                System.out.println( strKey +":"+ strValue );
//            }
//            writer.flush();
//        } catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            if ( null != writer) {try {writer.close();writer=null;}catch (Exception e){}}
//        }
//    }

//    private void readFile(){
//        BufferedReader reader = null;
//        try {
//           reader = new BufferedReader(new FileReader(userInfoFile));
//           String line = null;
//           while ((line = reader.readLine()) != null) {
//               String []strLines = line.split(" ");
//               // email , pw , email all count , write email count
//               if ( 4 == strLines.length ){
//                   mapUserInfo.put(strLines[0],line);
//               }
//           }
//        } catch (Exception e){
//            e.printStackTrace();
//        } finally {
//            if ( null != reader) {try {reader.close();reader=null;}catch (Exception e){}}
//        }
//    }
}
