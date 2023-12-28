package com.funnysalt.service;

import com.funnysalt.util.AES256;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.*;

public class ImapMaigrationState {

    public JSONArray getAllUserStateList(){
        Connection con = null ;
        Statement statement = null;
        ResultSet resultSet = null;

        JSONArray aryJObj = new JSONArray();

        try {

            con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
            con.setAutoCommit(true);


            // statement 객체 생성
            statement = con.createStatement();
            // RDB와 통신
            resultSet = statement.executeQuery("Select * FROM EMAIL_USER");


            while (resultSet.next()) {
                JSONObject jobj = new JSONObject();
                jobj.put("source_email",resultSet.getString("source_email"));
                jobj.put("target_email",resultSet.getString("target_email"));
                jobj.put("total_eml_count",resultSet.getString("total_eml_count"));
                jobj.put("download_eml_count",resultSet.getString("download_eml_count"));
                jobj.put("up_eml_count",resultSet.getString("up_eml_count"));
                jobj.put("download_mbox_name",resultSet.getString("download_mbox_name"));
                jobj.put("download_muid",resultSet.getString("download_muid"));
                jobj.put("up_mbox_name",resultSet.getString("up_mbox_name"));
                jobj.put("migration_state",resultSet.getString("migration_state"));

                aryJObj.add(jobj);

            }
        } catch (Exception se) {
            se.printStackTrace();
        }finally {
            if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
            if(statement != null){try{statement.close() ;} catch(SQLException se){}}
            if(con != null){try{con.close() ;} catch(SQLException se){}}
        }

        return aryJObj;
    }
}
