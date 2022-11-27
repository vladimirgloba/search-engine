package com.globa.search.engine.service.response;

public class ResponseError {

    private final boolean result = false;

    private String error;

    public ResponseError() {
        this.error = "Индексация не запущена";
    }

    public boolean isResult() {
        return result;
    }



    public void setError(String error) {
        this.error = error;
    }

    public Boolean getResult() {
        return result;
    }

    public String getError() {
        return error;
    }
}
