package com.globa.search.engine.service.response;

import org.springframework.stereotype.Component;

@Component
public class NoError {
    private boolean result=true;

    public NoError() {
    }

    public boolean isResult() {
        return result;
    }

    public void setResult() {
        this.result = true;
    }
}
