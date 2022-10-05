package com.globa.search.engine.controller;
import com.globa.search.engine.service.ForMultithreadedAddition;
import com.globa.search.engine.data.SiteDataService;
import com.globa.search.engine.service.ListOfObjectProperties;
import com.globa.search.engine.service.UserAgentProperties;
import com.globa.search.engine.service.response.NoError;
import com.globa.search.engine.service.response.ResponseError;
import com.globa.search.engine.service.response.ResponseForIndexing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Controller
public class IndexingController {
   @Autowired
    SiteDataService dataService;
   @Autowired
    ResponseError error;
   @Autowired
   private ResponseForIndexing forIndexing;
@Autowired
    NoError noError;
    @Autowired
    private ListOfObjectProperties listProperties;
@Autowired
    UserAgentProperties userAgentProperties;
    private Map<String,String>pathAndNameSite=new HashMap<>();
    @PersistenceContext
    EntityManager em;

     private  ExecutorService service = Executors.newSingleThreadExecutor();

    public IndexingController() {
    }

    @RequestMapping("/startIndexing")
    @ResponseBody
    public Object indexing(){

        getListOfSite();
        service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for(Map.Entry<String,String> entry:pathAndNameSite.entrySet()) {
            service.execute(new ForMultithreadedAddition(userAgentProperties,dataService, entry.getValue(), entry.getKey()));

        }

    service.shutdown();
if(!service.isTerminated()){
    return noError;
}
else
        return error;
    }
    private void getListOfSite(){
        pathAndNameSite=new HashMap<>();
        for(ListOfObjectProperties.Service service:listProperties.getServices()){
            pathAndNameSite.put(service.getUrl(),service.getName());
        }
    }
    @RequestMapping("/stopIndexing")
    @ResponseBody
    public Object stoIndexing(){
if(!service.isTerminated()){
    service.shutdownNow();
    return noError;
}else{
    return error;
}
    }



}

