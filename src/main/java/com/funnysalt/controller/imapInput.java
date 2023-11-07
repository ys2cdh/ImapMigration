package com.funnysalt.controller;

import com.funnysalt.bean.ImapServerInfoFile;
import com.funnysalt.bean.UserInfoFile;
import com.funnysalt.service.ImapTest;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class imapInput {

    @Autowired
    private UserInfoFile userInfoFile;

    @Autowired
    private ImapServerInfoFile imapServerInfoFile;

    @GetMapping("/")
    String home(Model model)
    {
        model.addAttribute("data","Hello ewfwefwef");
        return "hello";
    }


    @GetMapping("/imapInput")
    String imapInput(Model model)
    {

        return "imapInput";
    }

    @GetMapping("/imapServerInput")
    String imapServerInput(Model model)
    {

        return "imapServerInput";
    }

    @ResponseBody
    @PostMapping("/inputImapUserInfo")
    String inputImapUserInfo(@RequestParam(name = "userid") String strUserID, @RequestParam(name = "pw") String strPW)
    {
        System.out.println("user :" + strUserID + " pw : " + strPW);
        JSONObject jobj = new JSONObject();

        ImapTest imapTest = new ImapTest(imapServerInfoFile.getImapServerIP(),imapServerInfoFile.getImapServerPort(),imapServerInfoFile.getSSL(),strUserID,strPW);
        //로그인 실패시 return
        if (!imapTest.checkAuth()){
            jobj.put("code","-1");
            return jobj.toJSONString();
        }

        String strList = imapTest.getTwoDepthList();
        if (strList.isEmpty()){
            userInfoFile.addUserInfo(strUserID,strPW);
        }

        jobj.put("code","1");
        jobj.put("list",strList);
        return jobj.toJSONString();
    }


    @ResponseBody
    @PostMapping("/inputImapSeverBackend")
    String inputImapSeverBackend(@RequestParam(name = "ip") String strIP, @RequestParam(name = "port") String strPort)
    {
        System.out.println("IP :" + strIP + " port : " + strPort);
        JSONObject jobj = new JSONObject();

        imapServerInfoFile.save(strIP,strPort);

        jobj.put("code","1");

        return jobj.toJSONString();
    }
}
