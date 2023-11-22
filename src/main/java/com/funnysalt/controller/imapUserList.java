package com.funnysalt.controller;

import com.funnysalt.bean.UserInfoFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Iterator;

@Controller
public class imapUserList {

    @Autowired
    private UserInfoFile userInfoFile;

    @GetMapping("/imapUserList")
    String imapUserList(Model model)
    {

        ArrayList<String> aryList = new ArrayList<String>();

       Iterator<String> keys = userInfoFile.getSourceMapUserInfo().keySet().iterator();
       while (keys.hasNext()){
            aryList.add(keys.next());
        }

       model.addAttribute("list",aryList);
        return "imapUserList";
    }
}
