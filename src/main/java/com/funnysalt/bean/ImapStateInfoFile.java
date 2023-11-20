package com.funnysalt.bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class ImapStateInfoFile {

    private String filePath;
//    private Properties prop;

    private String svaePath;

    private Map<String,String> mapImapState = new HashMap<String,String>();

    public void init(String strFilePath)   {
        filePath = strFilePath;
        try {
            load();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void load() throws IOException {
        File f = new File(filePath);
//        if (null == prop) {
//            prop = new Properties();
//        }

        if (f.exists() && 0 < f.length()) {
            FileInputStream input=new FileInputStream(f);
            InputStreamReader reader=new InputStreamReader(input,"UTF-8");
            BufferedReader in=new BufferedReader(reader);
//            prop.load(in);
            in.close();
        }
    }

//    public Properties getProp() {
//        return prop;
//    }

//    public void save() throws IOException {
//        FileOutputStream fr = new FileOutputStream(filePath);
//        prop.store(fr, null);
//        fr.close();
//    }

    public void read(){
        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);

            // statement 객체 생성
            statement = con.createStatement();
            // RDB와 통신
            resultSet = statement.executeQuery("SELECT * FROM IMAP_MIGRATION_STATE");
            while (resultSet.next()) {
                mapImapState.put("total_eml_count", resultSet.getString(1));
                mapImapState.put("total_download_eml_count", resultSet.getString(2));
                mapImapState.put("total_up_eml_count", resultSet.getString(3));
                mapImapState.put("current_email", resultSet.getString(4));
                mapImapState.put("end_email", resultSet.getString(5));
                mapImapState.put("download_path", resultSet.getString(6));

                svaePath=resultSet.getString(6);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }

    public String getSavePath(){
        return svaePath;
    }

    public void savePath(String strPath) {
        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);

            // statement 객체 생성
            statement = con.createStatement();
            // RDB와 통신
            statement.executeUpdate("UPDATE IMAP_MIGRATION_STATE SET download_path='"+strPath+"'");

            svaePath=strPath;

        } catch (SQLException se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }

    public void saveTotalEmlCount(long nTotalCount) {
        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);

            // statement 객체 생성
            statement = con.createStatement();
            // RDB와 통신
            statement.executeUpdate("UPDATE IMAP_MIGRATION_STATE SET total_eml_count='"+nTotalCount+"'");


        } catch (SQLException se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }


}
