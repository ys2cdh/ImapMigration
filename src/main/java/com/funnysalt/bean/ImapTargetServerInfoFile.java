package com.funnysalt.bean;

import com.funnysalt.info.ImapServerInfoFile;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component(value="ImapTargetServerInfoFile")
public class ImapTargetServerInfoFile extends ImapServerInfoFile {

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
            resultSet = statement.executeQuery("SELECT trget_imap_IP,trget_imap_port FROM IMAP_SERVER_INFO");
            while (resultSet.next()) {
                imapServerIP= resultSet.getString(1);
                imapServerPort =resultSet.getInt(2);
            }
            if (993 == imapServerPort){
                bSSL = true;
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }
    @Override
    public void save(String strIP, String strPort) {
        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        imapServerIP=strIP;
        imapServerPort=Integer.parseInt(strPort);

        try {
            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);

            // statement 객체 생성
            statement = con.createStatement();
            // RDB와 통신
            statement.executeUpdate("UPDATE IMAP_SERVER_INFO SET trget_imap_IP='"+strIP+"', trget_imap_port="+strPort);


        } catch (SQLException se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }
    }
}
