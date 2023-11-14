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
			con = DriverManager.getConnection("jdbc:derby:/home/imapMigration;create=true");
			con.setAutoCommit(true);

			// statement 객체 생성
			statement = con.createStatement();
			// RDB와 통신
			resultSet = statement.executeQuery("SELECT * FROM EMAIL_USER");
			while (resultSet.next()) {

			}

		} catch (SQLException se) {
			//테이블이 없으면 생성
			if ( -1 < se.getMessage().indexOf("Table/View 'EMAIL_USER' does not exist") ){
				try {
					statement.addBatch(getCreateSql1());
					statement.addBatch(getCreateSql2());
					statement.addBatch(insertIMAP_SERVER_INFO());
					statement.addBatch(getCreateSql3());
					statement.addBatch(insertIMAP_MIGRATION_STATE());
					statement.executeBatch();
				} catch (SQLException e) {
					e.printStackTrace();
				}
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

	private static String getCreateSql1() {
		String strSql = "CREATE TABLE EMAIL_USER (\n" +
				"\tsource_email varchar(255)," +// COMMENT '원본 이메일 주소',\n" +
				"\tsource_pw varchar(255)," +// COMMENT '암호화된 패스워드',\n" +
				"\ttarget_email varchar(255)," +// COMMENT '복사본 이메일 주소',\n" +
				"\ttarget_pw varchar(255)," +// COMMENT '암호화된 패스워드',\n" +
				"\ttotal_eml_count BIGINT," +// COMMENT '원문 전체 카운트',\n" +
				"\tdownload_eml_count BIGINT," +// COMMENT '다운로드 한 원문 카운트',\n" +
				"\tup_eml_count BIGINT," +// COMMENT '업로드한  한 원문 카운트',\n" +
				"\tdownload_mbox_name varchar(255)," +// COMMENT '다운로드 중 메일함',\n" +
				"\tdownload_muid BIGINT," +// COMMENT '마지막 다운로드',\n" +
				"\tup_mbox_name varchar(255)," +// COMMENT '업로드 중 메일함',\n" +
				"\tup_muid BIGINT" +// COMMENT '마지막 업로드',\n" +
				")\n";
		return strSql;
	}
	private static String getCreateSql2() {
		//하나의 row만 가질 수 있다;
		String strSql = "CREATE TABLE IMAP_SERVER_INFO (\n" +
				"\tsource_imap_IP varchar(255)," +// COMMENT '원본 imap 서버 IP',\n" +
				"\tsource_imap_port INTEGER," +// COMMENT '원본 imap 서버 IP',\n" +
				"\ttrget_imap_IP varchar(255)," +// COMMENT '원본 imap 서버 IP',\n" +
				"\ttrget_imap_port INTEGER" +// COMMENT '원본 imap 서버 IP',\n" +
				")\n";
		return strSql;
	}


	private static String insertIMAP_SERVER_INFO() {
		//하나의 row만 가질 수 있다;
		String strSql = "INSERT INTO IMAP_SERVER_INFO VALUES('',0,'',0)";
		return strSql;
	}



	private static String getCreateSql3() {
				//하나의 row만 가질 수 있다
		String strSql ="CREATE TABLE IMAP_MIGRATION_STATE (\n" +
				"\ttotal_eml_count BIGINT," +// COMMENT '전체 메일 수',\n" +
				"\ttotal_download_eml_count BIGINT," +// COMMENT '전체 다운로드 한 원문 카운트',\n" +
				"\ttotal_up_eml_count BIGINT," +// COMMENT '전체 업로드한  한 원문 카운트',\n" +
				"\tcurrent_email LONG VARCHAR," +// COMMENT '마이그레이션 중인 이메일 주소',\n" +
				"\tend_email LONG VARCHAR" +// COMMENT '마이그레이션 완료 이메일 주소'\n" +
				")";
		return strSql;
	}

	private static String insertIMAP_MIGRATION_STATE() {
		//하나의 row만 가질 수 있다;
		String strSql = "INSERT INTO IMAP_MIGRATION_STATE VALUES(0,0,0,'','')";
		return strSql;
	}

}
