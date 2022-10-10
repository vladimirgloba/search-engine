package com.globa.search.engine.model;

import javax.persistence.*;

@Entity
@Table(name = "page")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(columnDefinition = "TEXT", name = "path")
    private String path;

    @Column(name = "code")
    private int code;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "site_id")
    private long site_id;

    public Page() {

    }

    public Page(String path, int code, String content, long site_id) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.site_id = site_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
