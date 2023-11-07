package com.funnysalt.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class MyApplicationRunner implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ApplicationArguments applicationArguments;

    @Autowired
    private UserInfoFile userInfoFile;

    @Autowired
    private ImapServerInfoFile imapServerInfoFile;

    @Autowired
    private ImapStateInfoFile imapStateInfoFile;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event){

//        String[] sourceArgs = applicationArguments.getSourceArgs();
//        System.out.println(sourceArgs[0] +" " +sourceArgs[1]);


//        ClassPathResource resourceimapServerInfo = new ClassPathResource("imapServerInfo.dat");
//        ClassPathResource resourceUserInfo = new ClassPathResource("userInfo.dat");
//        ClassPathResource resourceImapState= new ClassPathResource("imapState.prop");

        try {

            String rootPath = new ClassPathResource(".").getFile().getParentFile().getParentFile().getParentFile().getAbsolutePath();
            String imapServerInfo = rootPath+"/imapServerInfo.dat";
            String UserInfo = rootPath+"/userInfo.dat";
            String imapState = rootPath+"/imapState.prop";

            imapServerInfoFile.init(imapServerInfo);
            userInfoFile.init(UserInfo);
            imapStateInfoFile.init(imapState);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
