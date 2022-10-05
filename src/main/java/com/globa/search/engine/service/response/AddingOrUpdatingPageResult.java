package com.globa.search.engine.service.response;
import org.springframework.stereotype.Component;

@Component
public class AddingOrUpdatingPageResult {

    private boolean result = false;
    private String error = "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";

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

    public AddingOrUpdatingPageResult() {
    }
}
