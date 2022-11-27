package com.globa.search.engine.service;

import com.globa.search.engine.service.response.ResponseForSearchQueryFirstLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResponseForSearchQuery {
    @Autowired
    PageListFinder listFinder;

    private boolean result;
    private int count;
    private List<ResponseForSearchQueryFirstLevel> data = new ArrayList<>();

    public ResponseForSearchQuery() {
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

    public void getResponse(String searchQuery, String sitePath) {
        sitePath = sitePath + "/";
        this.data = listFinder.sortedPagesMapWithSQL(searchQuery, sitePath);
        this.count = listFinder.getTotalSize();
        this.result = !data.isEmpty();
    }

}
