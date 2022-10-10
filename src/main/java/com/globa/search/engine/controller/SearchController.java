package com.globa.search.engine.controller;

import com.globa.search.engine.service.response.ResponseForSearchQuery;
import com.globa.search.engine.service.response.ResponseForSearchQueryFromAllSite;
import com.globa.search.engine.service.response.SearchError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class SearchController {
    @Autowired
    private SearchError error;

    @Autowired
    private ResponseForSearchQuery searchQuery;

    @Autowired
    private ResponseForSearchQueryFromAllSite searchQueryFromAllSite;

    @RequestMapping("/search")
    @ResponseBody
    public Object getSearchQuery(@RequestParam(required = false, name = "query", defaultValue = "") String query,
                                 @RequestParam(required = false, name = "site", defaultValue = "") String site) {
        if (query.length() < 3 && site.length() < 5) {
            error.getForError();
            error.setError("Задан пустой поисковый запрос");
            return error;
        } else {
            if (site.length() > 4 && query.length() > 2) {
                System.out.println("в коротком запросе");
                searchQuery.getResponse(query, site);
                if (searchQuery.getData().size() == 0) {
                    error.getForError();
                    error.setError("По запросу ничего не найдено");
                    return error;
                } else {
                    return searchQuery;
                }
            } else {
                System.out.println("в длинном запросе");
                searchQueryFromAllSite.getResponse(query);
                if (searchQueryFromAllSite.getData().size() == 0) {
                    error.getForError();
                    error.setError("По запросу ничего не найдено");
                    return error;
                } else {
                    return searchQueryFromAllSite;
                }

            }


        }


    }
}
