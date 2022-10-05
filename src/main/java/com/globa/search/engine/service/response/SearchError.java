package com.globa.search.engine.service.response;

import org.springframework.stereotype.Component;

@Component
public class SearchError {
    private boolean result;
    private String error;

    public SearchError() {
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    public void getForError(){
        this.result=false;
        this.error="Задан пустой поисковый запрос";
    }
}
