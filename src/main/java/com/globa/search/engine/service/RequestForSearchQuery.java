package com.globa.search.engine.service;

import com.globa.search.engine.service.response.SearchError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestForSearchQuery {

    private static final Logger logger = LogManager.getLogger(RequestForSearchQuery.class);


    @Autowired
    private ResponseForSearchQuery responseForSearchQuery;//для одного сайта

    @Autowired
    private ResponseForSearchQueryFromAllSite responseForSearchQueryFromAllSite;//для нескольких сайтов


    public RequestForSearchQuery() {
    }

    private Object responseForOneSite(String siteName, String searchQuery) {
        responseForSearchQuery.getResponse(searchQuery, siteName);
        if (responseForSearchQuery.getData().size() == 0) {
            SearchError error = new SearchError();
            error.getForError();
            logger.error((char) 27 + "[31mWarning! " + "По запросу ничего не найдено" + (char) 27 + "[0m");
            error.setError("По запросу ничего не найдено");
            return error;
        } else {
            logger.info("успешное завершение обработки запроса \"/search\"");
            return responseForSearchQuery;
        }
    }

    private Object responseForAllSite(String searchQuery) {
        responseForSearchQueryFromAllSite.getResponse(searchQuery);
        if (responseForSearchQueryFromAllSite.getData().size() == 0) {
            SearchError error = new SearchError();
            error.getForError();
            error.setError("По запросу ничего не найдено");
            logger.error((char) 27 + "[31mWarning! " + "По запросу ничего не найдено" + (char) 27 + "[0m");
            return error;
        } else {
            logger.info("успешное завершение обработки запроса \"/search\"");
            System.out.println(responseForSearchQueryFromAllSite.getData().size());
            System.out.println(responseForSearchQueryFromAllSite.getCount() + " = count++++++++++++++++++++++++++");
            return responseForSearchQueryFromAllSite;
        }
    }

    public Object getResponse(String siteName, String searchQuery) {
        Object object = new Object();
        if (searchQuery.length() < 3) {
            SearchError error = new SearchError();
            error.getForError();
            error.setError("Задан пустой поисковый запрос");
            logger.error((char) 27 + "[31mWarning! " + "Задан пустой поисковый запрос" + (char) 27 + "[0m");
            object = error;
        }
        if (siteName.length() > 4 && searchQuery.length() > 2) {

            object = responseForOneSite(siteName, searchQuery);
        }
        if (siteName.length() == 0 && searchQuery.length() > 2) {

            object = responseForAllSite(searchQuery);
        }
        return object;
    }
}
