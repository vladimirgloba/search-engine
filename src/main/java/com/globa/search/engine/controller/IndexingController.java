package com.globa.search.engine.controller;

import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.service.ForMultithreadedAddition;
import com.globa.search.engine.service.ListOfObjectProperties;
import com.globa.search.engine.service.UserAgentProperties;
import com.globa.search.engine.service.response.NoError;
import com.globa.search.engine.service.response.ResponseError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Controller
public class IndexingController {
    private static final Logger logger = LogManager.getLogger(IndexingController.class);

    @Autowired
    private SiteDataService dataService;

    @Autowired
    private ResponseError error;

    @Autowired
    private NoError noError;

    @Autowired
    private UserAgentProperties userAgentProperties;

    @Autowired
    private ListOfObjectProperties listProperties;

    private Map<String, String> pathAndNameSite = new HashMap<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public IndexingController() {
    }


    @RequestMapping("/startIndexing")
    @ResponseBody
    public Object indexing() {
        logger.info("инициализация контроллера \"/startIndexing\"");
        getListOfSite();//получаем список сайтов для индексации
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (Map.Entry<String, String> entry : pathAndNameSite.entrySet()) {
            logger.info("инициализация многопоточного режима \"/startIndexing\"");
            executorService.execute(new ForMultithreadedAddition(userAgentProperties, dataService, entry.getValue(), entry.getKey()));
        }
        executorService.shutdown();
        if (!executorService.isTerminated()) {
            logger.info("успешное завершение работы контроллера \"/startIndexing\"");
            return noError;
        } else
            logger.error((char) 27 + "[31mWarning! " + "ошибка в условии:!service.isTerminated()" + (char) 27 + "[0m");
        return error;
    }

    @RequestMapping("/stopIndexing")
    @ResponseBody
    public Object stopIndexing() {
        logger.info("инициализация контроллера \"/stopIndexing\"");
        if (!executorService.isTerminated()) {
            executorService.shutdownNow();
            logger.info("успешное завершение работы контроллера \"/startIndexing\"");
            return noError;
        } else {
            logger.error((char) 27 + "[31mWarning! " + "ошибка в условии:!service.isTerminated()" + (char) 27 + "[0m");
            return error;
        }
    }

    private void getListOfSite() {
        pathAndNameSite = new HashMap<>();
        for (ListOfObjectProperties.Service service : listProperties.getServices()) {
            pathAndNameSite.put(service.getUrl(), service.getName());
        }
    }
}

