package com.funnysalt.imapmigration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.funnysalt.controller","com.funnysalt.bean"})
public class ImapmigrationApplication {

	//arg[0] imap 서버 정보 파일
	//arg[1] imap 사용자 로그인 정보 파일
	public static void main(String[] args) {
		if ( 2 > args.length){
			return;
		}
		SpringApplication.run(ImapmigrationApplication.class, args);
	}

}
