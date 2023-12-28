package com.funnysalt.controller;

import com.funnysalt.bean.ImapStateInfoFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.funnysalt.service.ImapMaigrationState;
@Controller
public class imapState {

    @Autowired
    private ImapStateInfoFile imapStateInfoFile;

    @GetMapping("/imapState")
    String imapState(Model model)
    {
        model.addAttribute("ing","");


//        Properties properties = imapStateInfoFile.getProp();
//        if (null != properties && properties.containsKey("ing...")){
//            model.addAttribute("ing",properties.get("ing..."));
//        }
//
//        ArrayList<String> aryList = new ArrayList<String>();
//        Enumeration em = properties.keys();
//        while(em.hasMoreElements()){
//            String str = (String)em.nextElement();
//            if (str.equals("ing...")){
//                continue;
//            }
//            aryList.add(str + ": " + properties.get(str));
////            System.out.println(str + ": " + properties.get(str));
//        }

//        model.addAttribute("completes",aryList);

        return "imapState";
    }

    @ResponseBody
    @GetMapping("/getAllImapState")
    String getAllImapState(){
        ImapMaigrationState imapMaigrationState =new ImapMaigrationState();
        return imapMaigrationState.getAllUserStateList().toJSONString();
    }
}
