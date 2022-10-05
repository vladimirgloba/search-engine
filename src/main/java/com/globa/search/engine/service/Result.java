package com.globa.search.engine.service;

import java.io.Serializable;

public class Result implements Serializable {
    private String pagePath;
    private String pageContent;
    private Float pageRank;

    public Result(String pagePath,String pageContent, Float pageRank) {
        this.pagePath = pagePath;
        this.pageContent=pageContent;
        this.pageRank = pageRank;
    }
    @Override
    public String toString() {
        return "Result{" +
                "path ='" + pagePath + '\'' +
                "content ='" + pageContent + '\'' +
                ", rank='" + pageRank + '\'' +
                '}';
    }
}
