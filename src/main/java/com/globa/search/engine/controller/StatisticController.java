package com.globa.search.engine.controller;

import com.globa.search.engine.service.response.StatisticFinal;
import com.globa.search.engine.service.response.Statistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class StatisticController {

    @Autowired
    StatisticFinal stat;
    private static final Logger logger = LogManager.getLogger(StatisticController.class);
    public StatisticController() {
    }

    @GetMapping("/statistics")
    @ResponseBody
    public Object getStatistics() {
        logger.info("инициализация контроллера \"/statistics\"");
        stat.getStatistic();
        logger.info("успешное завершение обработки запроса \"/statistics\"");
        return stat;
    }
}