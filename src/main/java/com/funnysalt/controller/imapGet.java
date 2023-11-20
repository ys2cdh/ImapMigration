package com.funnysalt.controller;

import com.funnysalt.bean.ImapSouceServerInfoFile;
import com.funnysalt.bean.ImapStateInfoFile;
import com.funnysalt.bean.ImapTargetServerInfoFile;
import com.funnysalt.bean.UserInfoFile;
import com.funnysalt.service.ImapTest;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class imapGet {

    @Autowired
    private UserInfoFile userInfoFile;

    @Autowired
    private ImapSouceServerInfoFile imapSouceServerInfoFile;

    @Autowired
    private ImapTargetServerInfoFile imapTargetServerInfoFile;

    @Autowired
    private ImapStateInfoFile imapStateInfoFile;

    @ResponseBody
    @GetMapping("/getServerInfo")
    String getServerInfo()
    {
        System.out.println("inputImapUserInfo");
        JSONObject jobj = new JSONObject();

        jobj.put("targetIP",imapTargetServerInfoFile.getImapServerIP());
        jobj.put("targetPort",imapTargetServerInfoFile.getImapServerPort());

        jobj.put("sourceIP",imapSouceServerInfoFile.getImapServerIP());
        jobj.put("sourcePort",imapSouceServerInfoFile.getImapServerPort());

        jobj.put("downloadPath",imapStateInfoFile.getSavePath());


        jobj.put("code","1");

        return jobj.toJSONString();
    }
}
