package com.globa.search.engine.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "status_time")
    private LocalDateTime status_time;
    @Column(columnDefinition="TEXT")
    private String last_error;
    @Column(name = "name")
    private String name;
    @Column(name = "url")
    private String url;

    public Site() {
    }

    public Site( Status status, LocalDateTime status_time, String last_error, String name, String url) {

        this.status = status;
        this.status_time = status_time;
        this.last_error = last_error;
        this.name = name;
        this.url = url;
    }



    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getStatus_time() {
        return status_time;
    }

    public void setStatus_time(LocalDateTime status_time) {
        this.status_time = status_time;
    }

    public String getLast_error() {
        return last_error;
    }

    public void setLast_error(String last_error) {
        this.last_error = last_error;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
