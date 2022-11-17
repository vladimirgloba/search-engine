package com.globa.search.engine.service.response;

import org.springframework.stereotype.Component;

@Component
public class ResponseError {

    Boolean result = false;

    String error = "Индексация не запущена";

    public ResponseError() {
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError() {
        this.error = "Индексация не запущена";
    }
}
