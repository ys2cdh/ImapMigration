package com.funnysalt.bean;

import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@Configuration
public class ImapServerInfoFile {

    private String imapServerIP;
    private int imapServerPort;
    private boolean bSSL=false;

    public void init(String strFilePath){

        File f = new File(strFilePath);
        if (f.exists() && 0 < f.length()) {
            readFile(f);
        }
    }

    private void readFile(File f) {
        BufferedReader reader = null;
        try {
           reader = new BufferedReader(new FileReader(f));
           String line = null;
           while ((line = reader.readLine()) != null) {
               String []strLines = line.split(" ");
               // imapServerIP , port , ssl( 없으면 false)
               if ( 1 < strLines.length ){
                   imapServerIP = strLines[0];
                   imapServerPort = Integer.parseInt(strLines[1]);
                   if ( 3 == strLines.length ) {
                       bSSL = Boolean.parseBoolean(strLines[2]);
                   }
               }
           }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if ( null != reader) {try {reader.close();reader=null;}catch (Exception e){}}
        }
    }

    public String getImapServerIP() {
        return imapServerIP;
    }

    public int getImapServerPort() {
        return imapServerPort;
    }

    public boolean getSSL() {
        return bSSL;
    }
}
