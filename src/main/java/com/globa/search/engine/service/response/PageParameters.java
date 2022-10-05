package com.globa.search.engine.service.response;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PageParameters {
    private String sitePath;
    private String path;
    private String content;
    private Map<String,Float> relevanceMap;

    public PageParameters() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Float> getRelevanceMap() {
        return relevanceMap;
    }

    public void setRelevanceMap(Map<String, Float> relevanceMap) {
        this.relevanceMap = relevanceMap;
    }

    public String getSitePath() {
        return sitePath;
    }

    public void setSitePath(String sitePath) {
        this.sitePath = sitePath;
    }
}
