package com.globa.search.engine.controller;

import com.globa.search.engine.service.response.StatisticFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class StatisticController {

    @Autowired
    StatisticFinal stat;

    public StatisticController() {
    }

    @GetMapping("/statistics")
    @ResponseBody
    public Object getStatistics() {
        stat.getStatistic();

        return stat;
    }
}