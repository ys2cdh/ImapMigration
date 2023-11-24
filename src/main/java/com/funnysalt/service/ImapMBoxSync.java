package com.funnysalt.service;

import com.funnysalt.bean.ImapSouceServerInfoFile;
import com.funnysalt.bean.ImapTargetServerInfoFile;
import com.funnysalt.util.AES256;
import com.funnysalt.util.BeanUtils;

import java.sql.*;
import java.util.ArrayList;

public class ImapMBoxSync {


    private ImapSouceServerInfoFile imapSouceServerInfoFile;
    private ImapTargetServerInfoFile imapTargetServerInfoFile;
    private String sourceEmail;

    private String sourcePW;

    private String targetEmail;
    private String targetPW;

    public ImapMBoxSync(String sourceEmail){
        this.sourceEmail = sourceEmail;

        imapSouceServerInfoFile = (ImapSouceServerInfoFile) BeanUtils.getBean("ImapSouceServerInfoFile");
        imapTargetServerInfoFile = (ImapTargetServerInfoFile) BeanUtils.getBean("ImapTargetServerInfoFile");

        System.out.println("imapMBoxSync source IP : " + imapSouceServerInfoFile.getImapServerIP() + " source port " + imapSouceServerInfoFile.getImapServerPort());

        getUserInfo();
        sync();
    }

    public void sync(){
        try {
            ImapWork imapWork = new ImapWork();

            imapWork.connect(imapSouceServerInfoFile.getImapServerIP(),imapSouceServerInfoFile.getImapServerPort(),imapSouceServerInfoFile.getSSL());
            imapWork.auth(sourceEmail,sourcePW);

            ArrayList<String> arySourceMBoxList = imapWork.listSync();
            imapWork.close();

            imapWork.connect(imapTargetServerInfoFile.getImapServerIP(),imapTargetServerInfoFile.getImapServerPort(),imapTargetServerInfoFile.getSSL());
            imapWork.auth(targetEmail,targetPW);

            ArrayList<String> aryTargetMBoxList = imapWork.listSync();

            for(String sboxName : arySourceMBoxList){
                if (false == aryTargetMBoxList.contains(sboxName)) {
                    int nResult = imapWork.crateMBox(sboxName);
                    System.out.println("Create MBox " + sboxName + " Result " + nResult);
                }
            }

            imapWork.close();


//            System.out.println("SMBox list " + arySourceMBoxList + " TMBox List " +aryTargetMBoxList );
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
            resultSet = statement.executeQuery("SELECT source_pw,target_email,target_pw FROM EMAIL_USER WHERE source_email='"+sourceEmail+"'");
            while (resultSet.next()) {

                pw = resultSet.getString(1);
                sourcePW = new AES256().decrypt(pw);
                targetEmail = resultSet.getString(2);
                pw = resultSet.getString(3);
                targetPW = new AES256().decrypt(pw);

                System.out.println("imapMBoxSync sync targetEmail : " + targetEmail );


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
