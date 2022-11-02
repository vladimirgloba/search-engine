package com.globa.search.engine.controller;

import com.globa.search.engine.service.response.AddingOrUpdatingPage;
import com.globa.search.engine.service.response.AddingOrUpdatingPageResult;
import com.globa.search.engine.service.response.NoError;
import com.globa.search.engine.service.response.PageSetOrInsert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class AddingController {
    @Autowired
    private AddingOrUpdatingPage result;

    @Autowired
    private PageSetOrInsert prowler;

    @Autowired
    private AddingOrUpdatingPageResult error;

    @Autowired
    private NoError noError;

    private static final Logger logger = LogManager.getLogger(AddingController.class);
    @PostMapping("indexPage")
    @ResponseBody
    public Object getSearchQuery(@RequestParam(required = false, name = "url", defaultValue = "") String url) {
        logger.info("инициализация контроллера \"/startIndexing\"");
        if (result.getResult(url)) {
            prowler.nativeQueryForPage(url);
            return noError;
        } else {
            logger.error((char) 27 + "[31mWarning! "+"ошибка при загрузке страницы" + (char)27 + "[0m");
            return error;
        }
    }
}
