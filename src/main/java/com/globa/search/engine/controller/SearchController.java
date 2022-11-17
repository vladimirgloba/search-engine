package com.globa.search.engine.controller;

import com.globa.search.engine.service.response.RequestForSearchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class SearchController {
    @Autowired
    RequestForSearchQuery requestForSearchQuery;

    @RequestMapping("/search")
    @ResponseBody
    public Object getSearchQuery(@RequestParam(required = false, name = "query", defaultValue = "") String query,
                                 @RequestParam(required = false, name = "site", defaultValue = "") String site) {
        return requestForSearchQuery.getResponse(site, query);

    }
}
