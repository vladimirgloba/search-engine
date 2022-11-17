package com.globa.search.engine.service.response;

import com.globa.search.engine.model.Status;
import org.springframework.stereotype.Component;


@Component
public class Detailed {
    private String url;
    private String name;
    private Status status;
    private Long statusTime;
    private String error;
    private Long pages;
    private Long lemmas;

    public Detailed() {
    }

    public Detailed(String url,
                    String name,
                    Status status,
                    Long statusTime,
                    String error,
                    Long pages,
                    Long lemmas) {
        this.url = url;
        this.name = name;
        this.status = status;
        this.statusTime = statusTime;
        this.error = error;
        this.pages = pages;
        this.lemmas = lemmas;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Long statusTime) {
        this.statusTime = statusTime;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }

    public Long getLemmas() {
        return lemmas;
    }

    public void setLemmas(Long lemmas) {
        this.lemmas = lemmas;
    }
}
