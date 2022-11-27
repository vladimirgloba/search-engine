package com.globa.search.engine.service.response;

public class Total {
    boolean isIndexing;
    private Long sites;
    private Long pages;
    private Long lemmas;

    public Total() {
    }

    public Total(Long sites, Long pages, Long lemmas, boolean isIndexing) {
        this.sites = sites;
        this.pages = pages;
        this.lemmas = lemmas;
        this.isIndexing = isIndexing;
    }

    public Long getSites() {
        return sites;
    }

    public void setSites(Long sites) {
        this.sites = sites;
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

    public boolean isIndexing() {
        return isIndexing;
    }

    public void setIndexing(boolean indexing) {
        isIndexing = indexing;
    }
}
