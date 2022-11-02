package com.globa.search.engine;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SearchEngineApplication {
    private static final Logger logger = LogManager.getLogger(SearchEngineApplication.class);

    public static void main(String[] args) {

        SpringApplication.run(SearchEngineApplication.class, args);
        logger.info("проверка загрузки приложения - успешно уровень логирования = "+logger.getLevel());
    }

}
