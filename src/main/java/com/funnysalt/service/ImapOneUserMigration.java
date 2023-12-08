package com.funnysalt.service;

import com.funnysalt.bean.ImapSouceServerInfoFile;
import com.funnysalt.bean.ImapStateInfoFile;
import com.funnysalt.bean.ImapTargetServerInfoFile;
import com.funnysalt.util.AES256;
import com.funnysalt.util.BeanUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class ImapOneUserMigration implements Runnable{

    private String sourceEmail;

    private String sourcePW;

    private String downloadMBoxName;
    private long startUID;

    private String targetEmail;


    private ImapSouceServerInfoFile imapSouceServerInfoFile;
    private ImapStateInfoFile imapStateInfoFile;
    private ImapTargetServerInfoFile imapTargetServerInfoFile;

    public ImapOneUserMigration(String sourceEmail){
        this.sourceEmail=sourceEmail;

        imapSouceServerInfoFile = (ImapSouceServerInfoFile) BeanUtils.getBean("ImapSouceServerInfoFile");
        imapStateInfoFile = (ImapStateInfoFile) BeanUtils.getBean("ImapStateInfoFile");
        imapTargetServerInfoFile = (ImapTargetServerInfoFile) BeanUtils.getBean("ImapTargetServerInfoFile");

        getUserInfo();
    }
    @Override
    public void run() {

        // 소스 메일함 정보를 가져온다
        ImapWork imapSourceWork = new ImapWork();

        imapSourceWork.connect(imapSouceServerInfoFile.getImapServerIP(),imapSouceServerInfoFile.getImapServerPort(),imapSouceServerInfoFile.getSSL());
        imapSourceWork.auth(sourceEmail,sourcePW);

        try {
            ArrayList<String> arySourceMBoxList = imapSourceWork.listSync();

            //메일함 정렬
            Collections.sort(arySourceMBoxList);


            for (String mBoxName : arySourceMBoxList){

                // downloadMBoxName 값이 null 이거나 또는 "" 이면 처음 메일함 부터 작업 하면 된다
                if ( null != downloadMBoxName && !downloadMBoxName.isEmpty() && !downloadMBoxName.equals(mBoxName)){
                    continue;
                }

                // 다운로드 시작
                ImapMBoxEmlDownlod imapMBoxEmlDownlod = new ImapMBoxEmlDownlod(sourceEmail,mBoxName,startUID);
                imapMBoxEmlDownlod.run();

                //업로드 시작
                ImapMBoxEmlUpload imapMBoxEmlUpload = new ImapMBoxEmlUpload(targetEmail,mBoxName);
                imapMBoxEmlUpload.run();
            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            resultSet = statement.executeQuery("SELECT source_pw,download_mbox_name,download_muid,target_email FROM EMAIL_USER WHERE source_email='"+sourceEmail+"'");
            while (resultSet.next()) {

                pw = resultSet.getString(1);
                sourcePW = new AES256().decrypt(pw);

                downloadMBoxName = resultSet.getString(2);

                startUID = resultSet.getLong(3);

                targetEmail = resultSet.getString(4);
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
