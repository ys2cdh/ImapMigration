package com.funnysalt.bean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImapServerInfoConfig {

    @Bean(name="ImapSouceServerInfoFile")
    public ImapServerInfoFile ImapSouceServerInfoFile(String strFilePath){
        ImapServerInfoFile imapServerInfoFile =new ImapServerInfoFile();
        imapServerInfoFile.init(strFilePath);

        return imapServerInfoFile;
    }

    @Bean(name="ImapTargetServerInfoFile")
    public ImapServerInfoFile ImapTargetServerInfoFile(String strFilePath){
        ImapServerInfoFile imapServerInfoFile =new ImapServerInfoFile();
        imapServerInfoFile.init(strFilePath);

        return imapServerInfoFile;
    }
}
