package com.funnysalt.service;

import com.funnysalt.bean.ImapSouceServerInfoFile;
import com.funnysalt.bean.ImapStateInfoFile;
import com.funnysalt.util.AES256;
import com.funnysalt.util.BeanUtils;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

//메일함을 지정하면 해당 메일함에 있는 원문을 지정된 폴더에 저장
public class ImapMBoxEmlDownlod implements Runnable {

    String mBoxName;
    private String sourceEmail;

    private String sourcePW;

    // 해당 uid 부터 메일을 다운로드 받는다
    private long startUID;

    private ImapSouceServerInfoFile imapSouceServerInfoFile;
    private ImapStateInfoFile imapStateInfoFile;
    public ImapMBoxEmlDownlod(String sourceEmail,String mBoxName,long startUID){
        this.sourceEmail=sourceEmail;
        this.mBoxName=mBoxName;
        this.startUID=startUID;

        imapSouceServerInfoFile = (ImapSouceServerInfoFile) BeanUtils.getBean("ImapSouceServerInfoFile");
        imapStateInfoFile = (ImapStateInfoFile) BeanUtils.getBean("ImapStateInfoFile");

        getUserInfo();


    }

    @Override
    public void run() {

        ImapWork imapWork = new ImapWork();

        imapWork.connect(imapSouceServerInfoFile.getImapServerIP(),imapSouceServerInfoFile.getImapServerPort(),imapSouceServerInfoFile.getSSL());
        imapWork.auth(sourceEmail,sourcePW);

        try {
            boolean bResult = imapWork.select(mBoxName);
            new File(imapStateInfoFile.getSavePath()+"/"+sourceEmail+"/"+mBoxName).mkdirs();
            ArrayList<Long> aryUIDs = imapWork.getAfterFewTimes(startUID);

            for(long uid : aryUIDs){

                // 업로드 했는지 체크
                File f = new File(imapStateInfoFile.getSavePath()+"/"+sourceEmail+"/"+mBoxName+"/"+uid+".emlcfg");

                // 다운로드에 성공 하면 관련 데이터 업데이트
                if (!f.exists() && imapWork.downloadEml(imapStateInfoFile.getSavePath()+"/"+sourceEmail+"/"+mBoxName,uid)){
                    // 현재 다운로드 폴더 및 마지막 uid 값 업데이트
                    updateDBDownload(uid);
                }
            }
//            imapWork.downloadEml(imapStateInfoFile.getSavePath()+"/"+mBoxName, imapWork.getAllUIDs());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean updateDBDownload(long uid){

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
            statement.execute("UPDATE EMAIL_USER SET download_eml_count=download_eml_count+1 , download_mbox_name='"+mBoxName+"', download_muid="+uid+"where source_email='"+sourceEmail+"'");

            statement.execute("UPDATE IMAP_MIGRATION_STATE SET total_download_eml_count=total_download_eml_count+1");


        } catch (Exception se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }

        return false;
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
            resultSet = statement.executeQuery("SELECT source_pw FROM EMAIL_USER WHERE source_email='"+sourceEmail+"'");
            while (resultSet.next()) {

                pw = resultSet.getString(1);
                sourcePW = new AES256().decrypt(pw);

            }

        } catch (Exception se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }
}
