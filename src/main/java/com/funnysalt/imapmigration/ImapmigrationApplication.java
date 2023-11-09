package com.funnysalt.imapmigration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.sql.*;

@SpringBootApplication
@ComponentScan(basePackages = {"com.funnysalt.controller","com.funnysalt.bean"})
public class ImapmigrationApplication {

	//arg[0] imap 서버 정보 파일
	//arg[1] imap 사용자 로그인 정보 파일
	public static void main(String[] args) {
//		if ( 2 > args.length){
//			return;
//		}

		Connection con = null ;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver") ;
			con = DriverManager.getConnection("jdbc:derby:test;create=true");

			// statement 객체 생성
			statement = con.createStatement();
			// RDB와 통신
			resultSet = statement.executeQuery("SELECT * FROM EMAILUSER");
			while (resultSet.next()) {

			}

		} catch (SQLException se) {
			//테이블이 없으면 생성
			if ( -1 < se.getMessage().indexOf("Table/View 'EMAILUSER' does not exist") ){

			}
			se.printStackTrace();
		} catch(ClassNotFoundException e){
			System.out.println("JDBC Driver not found in CLASSPATH") ;
		}finally {
			if(resultSet != null){try{resultSet.close() ;} catch(SQLException se){}	}
			if(statement != null){try{statement.close() ;} catch(SQLException se){}}
			if(con != null){try{con.close() ;} catch(SQLException se){}}
		}


		SpringApplication.run(ImapmigrationApplication.class, args);
	}

	private void aaa(){
		String strSql="CREATE TABLE EMAILUSER (\n" +
				"\tsource_email varchar(255) COMMENT '원본 이메일 주소',\n" +
				"\tsource_pw varchar(255) COMMENT '암호화된 패스워드',\n" +
				"\ttarget_email varchar(255) COMMENT '복사본 이메일 주소',\n" +
				"\ttarget_pw varchar(255) COMMENT '암호화된 패스워드',\n" +
				"\ttotal_eml_count long COMMENT '원문 전체 카운트',\n" +
				"\tdownload_eml_count long COMMENT '다운로드 한 원문 카운트',\n" +
				"\tup_eml_count long COMMENT '업로드한  한 원문 카운트',\n" +
				"\tdownload_mbox_name varchar(255) COMMENT '다운로드 중 메일함',\n" +
				"\tdownload_muid long COMMENT '마지막 다운로드',\n" +
				"\tup_mbox_name varchar(255) COMMENT '업로드 중 메일함',\n" +
				"\tup_muid long COMMENT '마지막 업로드',\n" +
				");";
	}

}
