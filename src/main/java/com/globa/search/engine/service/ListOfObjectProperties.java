package com.globa.search.engine.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "get")
public class ListOfObjectProperties {

    private List<Service> services;

    public ListOfObjectProperties() {
    }

    public ListOfObjectProperties(List<Service> services) {
        this.services = services;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public static class Service {
        private String name;
        private String url;

        public Service() {
        }

        public Service(String name, String url) {
            this.name = name;
            this.url = url;
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
}
