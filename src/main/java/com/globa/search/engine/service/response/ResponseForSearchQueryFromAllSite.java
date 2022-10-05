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
    private int count=0;
    private List<ResponseForSearchQueryFirstLevel> data=new ArrayList<>();
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
    public void getResponse(String query){
        List<Site>allSites=dataService.finedAllSites();
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++"+allSites.size());
        for(Site site:allSites){
            System.out.println(site.getUrl());
            this.data.addAll(listFinder.sortedPagesMapWithSQL(query, site.getUrl()));
            this.count=this.count+listFinder.getTotalSize();
        }
            this.result=data.isEmpty()?false:true;
        if(result){

            data=data.stream().sorted(Collections.reverseOrder((o1, o2)->o1.getRelevance().
                    compareTo(o2.getRelevance()))).collect(Collectors.toList());
        }
    }
}
