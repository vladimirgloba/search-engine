package com.globa.search.engine.controller;
import com.globa.search.engine.service.response.AddingOrUpdatingPage;
import com.globa.search.engine.service.response.AddingOrUpdatingPageResult;
import com.globa.search.engine.service.response.NoError;
import com.globa.search.engine.service.response.PageSetOrInsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AddingController {
    @Autowired
    AddingOrUpdatingPage result;
    @Autowired
    PageSetOrInsert prowler;
    @Autowired
    AddingOrUpdatingPageResult error;
    @Autowired
    NoError noError;
    @PostMapping("indexPage")
    @ResponseBody
    public Object getSearchQuery(@RequestParam(required=false, name = "url",defaultValue="") String url){
       if(result.getResult(url)){
           prowler.nativeQueryForPage(url);
           return noError;
       }
       else {
            return error;
        }
    }
}
