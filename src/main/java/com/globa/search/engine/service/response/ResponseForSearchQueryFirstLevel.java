package com.globa.search.engine.service.response;

import com.globa.search.engine.data.SiteDataService;
import org.springframework.stereotype.Component;

@Component
public class ResponseForSearchQueryFirstLevel {

   private SiteDataService dataService;
   private String site;
   private String siteName;
   private String uri;
   private String title;
   private String snippet;
   private Float relevance;

    public ResponseForSearchQueryFirstLevel() {
    }

    public void getData(SiteDataService dataService, String site, String siteName,
                        String uri, String title, String snippet, Float relevance) {
        this.dataService = dataService;
        this.site = site;
        this.siteName = siteName;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public Float getRelevance() {
        return relevance;
    }

    public void setRelevance(Float relevance) {
        this.relevance = relevance;
    }
}
