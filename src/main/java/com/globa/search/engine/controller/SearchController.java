package com.globa.search.engine.controller;

import com.globa.search.engine.service.response.ResponseForSearchQuery;
import com.globa.search.engine.service.response.ResponseForSearchQueryFromAllSite;
import com.globa.search.engine.service.response.SearchError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(SearchController.class);

    @RequestMapping("/search")
    @ResponseBody
    public Object getSearchQuery(@RequestParam(required = false, name = "query", defaultValue = "") String query,
                                 @RequestParam(required = false, name = "site", defaultValue = "") String site) {
        logger.info("инициализация контроллера \"/search\"");
        if (query.length() < 3 && site.length() < 5) {
            error.getForError();
            error.setError("Задан пустой поисковый запрос");
            logger.error((char) 27 + "[31mWarning! "+"Задан пустой поисковый запрос" + (char)27 + "[0m");
            return error;
        } else {
            if (site.length() > 4 && query.length() > 2) {
                searchQuery.getResponse(query, site);
                if (searchQuery.getData().size() == 0) {
                    error.getForError();
                    logger.error((char) 27 + "[31mWarning! "+"По запросу ничего не найдено" + (char)27 + "[0m");
                    error.setError("По запросу ничего не найдено");
                    return error;
                } else {
                    logger.info("успешное завершение обработки запроса \"/search\"");
                    return searchQuery;
                }
            } else {
                searchQueryFromAllSite.getResponse(query);
                if (searchQueryFromAllSite.getData().size() == 0) {
                    error.getForError();
                    error.setError("По запросу ничего не найдено");
                    logger.error((char) 27 + "[31mWarning! "+"По запросу ничего не найдено" + (char)27 + "[0m");
                    return error;
                } else {
                    logger.info("успешное завершение обработки запроса \"/search\"");
                    return searchQueryFromAllSite;
                }

            }


        }


    }
}
