package com.funnysalt.bean;

import com.funnysalt.service.ImapMBoxEmlDownlod;
import com.funnysalt.service.ImapMBoxEmlUpload;
import com.funnysalt.service.ImapMBoxSync;
import com.funnysalt.util.ActiveTasksThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

@Component
public class MyApplicationRunner implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ApplicationArguments applicationArguments;

    @Autowired
    private UserInfoFile userInfoFile;

    @Autowired
    private ImapSouceServerInfoFile imapSouceServerInfoFile;

    @Autowired
    private ImapTargetServerInfoFile imapTargetServerInfoFile;

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
            String imapSourceServerInfo = rootPath+"/imapSourceServerInfo.dat";
            String imapTargetServerInfo = rootPath+"/imapTargetServerInfo.dat";
            String UserInfo = rootPath+"/userInfo.dat";
            String imapState = rootPath+"/imapState.prop";


//            imapSouceServerInfoFile.init(imapSourceServerInfo);
//            imapTargetServerInfoFile.init(imapTargetServerInfo);
            imapSouceServerInfoFile.read();
            imapTargetServerInfoFile.read();
            userInfoFile.read();
//            userInfoFile.init(UserInfo);
            imapStateInfoFile.read();





            //test
//            ImapMBoxSync imapMBoxSync = new ImapMBoxSync("ys2cdh@smart-m.co.kr");

            //test
//            ActiveTasksThreadPool activeTasksThreadPool = new ActiveTasksThreadPool(5,5,10, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
//            ImapMBoxEmlDownlod imapMBoxEmlDownlod = new ImapMBoxEmlDownlod("ys2cdh@smart-m.co.kr","INBOX");
//            activeTasksThreadPool.execute(imapMBoxEmlDownlod);
//            ImapMBoxEmlUpload imapMBoxEmlUpload =  new ImapMBoxEmlUpload("ys2cdh@mail36524.net","INBOX");
//            activeTasksThreadPool.execute(imapMBoxEmlUpload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
