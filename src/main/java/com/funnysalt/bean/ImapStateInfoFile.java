package com.funnysalt.bean;

import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.util.Properties;

@Configuration
public class ImapStateInfoFile {

    private String filePath;
    private Properties prop;

    public void init(String strFilePath)   {
        filePath = strFilePath;
        try {
            load();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void load() throws IOException {
        File f = new File(filePath);
        if (null == prop) {
            prop = new Properties();
        }

        if (f.exists() && 0 < f.length()) {
            FileInputStream input=new FileInputStream(f);
            InputStreamReader reader=new InputStreamReader(input,"UTF-8");
            BufferedReader in=new BufferedReader(reader);
            prop.load(in);
            in.close();
        }
    }

    public Properties getProp() {
        return prop;
    }

    public void save() throws IOException {
        FileOutputStream fr = new FileOutputStream(filePath);
        prop.store(fr, null);
        fr.close();
    }
}
