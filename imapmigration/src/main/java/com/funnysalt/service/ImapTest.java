package com.funnysalt.service;

import com.funnysalt.util.UTF7Coder;

import java.util.ArrayList;

public class ImapTest {

    ImapWork imapWork;
    String strTwoDepthList="";
    boolean bAuth=false;

    public ImapTest(String serverIP,int port,boolean ssl,String id,String pw){
        imapWork = new ImapWork();
//        imapWork.connect("imap.daum.net",993,true);
//        imapWork.connect("imap.naver.com",993,true);
        imapWork.connect(serverIP,port,ssl);
        bAuth = imapWork.auth(id,pw);


        try {
            ArrayList<String> aryBoxList = imapWork.listSync();
            for ( String box : aryBoxList){
                System.out.println(box + " " + box.split("/").length);
                if (3 <= box.split("/").length){
                    strTwoDepthList += UTF7Coder.d(box) + " , ";
                }
                imapWork.getBoxMailCount(box);
            }
//            System.out.println(imapWork.listSync());

        } catch (Exception e){
            e.printStackTrace();
        }



        if ( 1 < strTwoDepthList.length()){
            strTwoDepthList=strTwoDepthList.substring(0,strTwoDepthList.length()-2);
        }

       imapWork.close();
    }

    public boolean checkAuth(){return bAuth;}


    public String getTwoDepthList() {
        return strTwoDepthList;
    }
}
