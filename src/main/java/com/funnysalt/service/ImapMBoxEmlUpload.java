package com.funnysalt.service;

import com.funnysalt.bean.ImapSouceServerInfoFile;
import com.funnysalt.bean.ImapStateInfoFile;
import com.funnysalt.bean.ImapTargetServerInfoFile;
import com.funnysalt.util.AES256;
import com.funnysalt.util.BeanUtils;

import java.io.File;
import java.io.FileFilter;
import java.sql.*;

public class ImapMBoxEmlUpload implements Runnable{

    private String sourceEmail;
    String mBoxName;
    private String targetEmail;

    private String targetPW;

    private ImapTargetServerInfoFile imapTargetServerInfoFile;
    private ImapStateInfoFile imapStateInfoFile;

    public ImapMBoxEmlUpload(String targetEmail,String mBoxName){
        this.targetEmail=targetEmail;
        this.mBoxName=mBoxName;

        imapTargetServerInfoFile = (ImapTargetServerInfoFile) BeanUtils.getBean("ImapTargetServerInfoFile");
        imapStateInfoFile = (ImapStateInfoFile) BeanUtils.getBean("ImapStateInfoFile");

        getUserInfo();


    }

    private void getUserInfo(){
        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);

            String pw;
            // statement 객체 생성
            statement = con.createStatement();
            // RDB와 통신
            resultSet = statement.executeQuery("SELECT source_email,target_pw FROM EMAIL_USER WHERE target_Email='"+targetEmail+"'");
            while (resultSet.next()) {

                sourceEmail = resultSet.getString(1);
                pw = resultSet.getString(2);
                targetPW = new AES256().decrypt(pw);

            }

        } catch (Exception se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }

    @Override
    public void run() {
        ImapWork imapWork = new ImapWork();

        imapWork.connect(imapTargetServerInfoFile.getImapServerIP(),imapTargetServerInfoFile.getImapServerPort(),imapTargetServerInfoFile.getSSL());
        imapWork.auth(targetEmail,targetPW);

        try {
            boolean bResult = imapWork.select(mBoxName);

            String strEmlPath = imapStateInfoFile.getSavePath()+"/"+sourceEmail+"/"+mBoxName;
            File[] files = new File(strEmlPath).listFiles(new FileFilter() {
                public boolean accept(File f)
                {
                    return f.getName().endsWith("eml");
                }
            });

            for (File f : files){
                imapWork.uploadEml(f);

                //DB update
                updateDBUpload();
            }

//            imapWork.uploadEml(imapStateInfoFile.getSavePath()+"/"+mBoxName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean updateDBUpload(){

        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);

            String pw;
            // statement 객체 생성
            statement = con.createStatement();
            // 다운로드 카운트 1 증가
            statement.execute("UPDATE EMAIL_USER SET up_eml_count=up_eml_count+1 , up_mbox_name='"+mBoxName+"' where source_email='"+sourceEmail+"'");

            statement.execute("UPDATE IMAP_MIGRATION_STATE SET total_up_eml_count=total_up_eml_count+1");


        } catch (Exception se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }

        return false;
    }
}
