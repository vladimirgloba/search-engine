package com.globa.search.engine.service.response;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.model.Site;
import com.globa.search.engine.service.PageListFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResponseForSearchQueryFromAllSite {

    @Autowired
    PageListFinder listFinder;

    @Autowired
    SiteDataService dataService;

    private boolean result;

    private int count = 0;

    private List<ResponseForSearchQueryFirstLevel> data = new ArrayList<>();

    public ResponseForSearchQueryFromAllSite() {
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ResponseForSearchQueryFirstLevel> getData() {
        return data;
    }

    public void setData(List<ResponseForSearchQueryFirstLevel> data) {
        this.data = data;
    }

    public void getResponse(String query) {
        List<Site> allSites = dataService.finedAllSites();


        for (Site site : allSites) {
            List<ResponseForSearchQueryFirstLevel> firstLevels = listFinder.sortedPagesMapWithSQL(query, site.getUrl());
            result = site.getStatus().toString().equals("INDEXED");
            if (!firstLevels.isEmpty()) {
                this.data.addAll(firstLevels);
                this.count = this.count + listFinder.getTotalSize();
            }
        }

        if (!data.isEmpty()) {

            data = data.stream().sorted(Collections.reverseOrder((o1, o2) -> o1.getRelevance().
                    compareTo(o2.getRelevance()))).collect(Collectors.toList());
        }
    }
}
