package com.globa.search.engine.service;

import com.globa.search.engine.data.SiteDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticFinal {

    @Autowired
    SiteDataService dataService;
    @Autowired
    Statistics statistics;
    private Boolean result = true;

    public StatisticFinal() {

    }

    public void getStatistic() {
        statistics.getStatistic();
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }
}
