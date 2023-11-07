package com.funnysalt.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;

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

        String[] sourceArgs = applicationArguments.getSourceArgs();
        System.out.println(sourceArgs[0] +" " +sourceArgs[1]);

        imapServerInfoFile.init(sourceArgs[0]);
        userInfoFile.init(sourceArgs[1]);

        File file = new File(sourceArgs[0]);
        imapStateInfoFile.init(file.getParentFile().getAbsolutePath()+"/imapState.prop");
    }
}
